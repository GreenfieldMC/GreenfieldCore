package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.serviceprovider.Nullable;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockRoleAssignment;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockSearchResult;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldRedblockApiService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IUserService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.greenfieldmc.core.Util.*;
import static net.greenfieldmc.core.redblock.services.RedblockDynmapLoader.*;
import static net.greenfieldmc.core.redblock.services.RedblockServiceHelpers.*;

/**
 * Api backed redblock service. The api is the source of truth; this service keeps an in-memory cache of the active
 * (incomplete or pending) redblocks and keeps the world (cube, sign, display entity) and Dynmap in sync with the api.
 * <p>
 * All returned futures complete off the main thread. World changes are hopped onto the main thread, so never join
 * these futures from the main thread.
 */
public class RedblockServiceV2Impl implements IRedblockServiceV2 {

    private static final int CACHE_LOAD_PAGE_SIZE = 50;

    private final IGreenfieldCorePlugin plugin;
    private final Logger logger;
    private final @Nullable IDynmapService dynmapService;
    private final IGreenfieldRedblockApiService redblockApiService;
    private final IGreenfieldUserApiService userApiService;
    private final IUserService userService;
    private final IRedblockConfigService configService;

    // only active (incomplete or pending, not deleted) redblocks are kept in here
    private final Map<String, GfRedblock> activeRedblocks = new ConcurrentHashMap<>();

    // dynmapLoader is only taken so the marker sets are created before this service adds markers
    public RedblockServiceV2Impl(IGreenfieldCorePlugin plugin, Logger logger, @Nullable IDynmapService dynmapService, RedblockDynmapLoader dynmapLoader, IGreenfieldRedblockApiService redblockApiService, IGreenfieldUserApiService userApiService, IUserService userService, IRedblockConfigService configService) {
        this.plugin = plugin;
        this.logger = logger;
        this.dynmapService = dynmapService;
        this.redblockApiService = redblockApiService;
        this.userApiService = userApiService;
        this.userService = userService;
        this.configService = configService;

        // the module cannot work with a missing or partial cache, so fail to enable instead. Nothing in the load hops
        // onto the main thread, so blocking here is safe
        try {
            loadCache().join();
        } catch (CompletionException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("Failed to load RedBlocks from the api: " + cause.getMessage(), cause);
        }
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> getRedblock(String redblockKey) {
        var cached = activeRedblocks.get(redblockKey);
        if (cached != null) {
            fetchRedblock(redblockKey).thenAccept(result -> {
                if (result.isFailure()) logger.warning("Failed to refresh redblock cache for key " + redblockKey + ": " + result.getErrorMessage());
            });
            return CompletableFuture.completedFuture(Result.success(cached));
        }
        return fetchRedblock(redblockKey);
    }

    @Override
    public List<GfRedblock> getRedblocks(Predicate<GfRedblock> filter) {
        if (filter == null) return List.copyOf(activeRedblocks.values());
        return activeRedblocks.values().stream().filter(filter).toList();
    }

    @Override
    public CompletableFuture<Result<GfRedblockSearchResult>> searchRedblocks(World world, GfRedblockSearchRequest searchRequest) {
        var projectKey = configService.getProjectKey(world);
        if (projectKey == null) return failed(RedblockMessages.ERROR_WORLD_NOT_MAPPED.apply(world.getName()));
        return redblockApiService.searchRedblocks(projectKey, searchRequest);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> createRedblock(String message, Player createdBy, Location location, @Nullable List<GfUser> userAssignments, @Nullable List<String> roleAssignments) {
        var projectKey = configService.getProjectKey(location.getWorld());
        if (projectKey == null) return failed(RedblockMessages.ERROR_WORLD_NOT_MAPPED.apply(location.getWorld().getName()));

        var userIds = userAssignments == null ? List.<Long>of() : userAssignments.stream().map(GfUser::getUserId).toList();
        var roles = roleAssignments == null ? List.<String>of() : roleAssignments;

        return resolveUserId(createdBy)
                .thenCompose(ifSuccess(userId -> redblockApiService.createRedblock(projectKey, location, message, STATUS_INCOMPLETE, userId, userIds, roles)))
                .thenCompose(ifSuccess((rb) -> createRedblockDisplay(rb, userAssignments, roleAssignments)));
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> editRedblock(String redblockKey, @Nullable String message, @Nullable List<GfUser> userAdditions, @Nullable List<GfUser> userRemovals, @Nullable List<String> roleAdditions, @Nullable List<String> roleRemovals, Player editedBy) {
        return fetchRedblock(redblockKey).thenCompose(ifSuccess(redblock -> resolveUserId(editedBy).thenCompose(ifSuccess(userId -> {
            var changes = new ArrayList<CompletableFuture<? extends Result<?>>>();

            if (message != null && !message.isBlank() && !message.equals(redblock.getMessage()))
                changes.add(redblockApiService.updateRedblock(redblockKey, message, userId));

            if (userAdditions != null) userAdditions.forEach(user -> changes.add(redblockApiService.addRedblockUserAssignment(redblockKey, user.getUserId(), userId)));
            if (userRemovals != null) userRemovals.forEach(user -> changes.add(redblockApiService.removeRedblockUserAssignment(redblockKey, user.getUserId())));
            if (roleAdditions != null) roleAdditions.forEach(role -> changes.add(redblockApiService.addRedblockRoleAssignment(redblockKey, role, userId)));
            if (roleRemovals != null) roleRemovals.forEach(role -> changes.add(redblockApiService.removeRedblockRoleAssignment(redblockKey, role)));

            return CompletableFuture.allOf(changes.toArray(CompletableFuture[]::new)).thenCompose(v -> {
                var errors = changes.stream().map(CompletableFuture::join).filter(Result::isFailure).map(Result::getErrorMessage).toList();
                // re-render with the assignments the api now has, so the world matches whatever did get saved
                return fetchRedblock(redblockKey)
                        .thenCompose(ifSuccess(rb -> resolveAssignedUsers(rb).thenCompose(users -> createRedblockDisplay(rb, users, rb.getRoleAssignments().stream().map(GfRedblockRoleAssignment::getRoleName).toList()))))
                        .thenApply(rendered -> errors.isEmpty() || rendered.isFailure()
                                ? rendered
                                : Result.failure(RedblockMessages.ERROR_EDIT_PARTIALLY_SAVED.apply(redblockKey, errors)));
            });
        }))));
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> completeRedblock(String redblockKey, Player completedBy) {
        return changeStatus(redblockKey, completedBy, STATUS_PENDING);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> approveRedblock(String redblockKey, Player approvedBy) {
        return changeStatus(redblockKey, approvedBy, STATUS_APPROVED);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> denyRedblock(String redblockKey, Player deniedBy) {
        return changeStatus(redblockKey, deniedBy, STATUS_INCOMPLETE);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> deleteRedblock(String redblockKey, Player deletedBy) {
        return fetchRedblock(redblockKey).thenCompose(ifSuccess(redblock -> resolveUserId(deletedBy)
                .thenCompose(ifSuccess(userId -> redblockApiService.deleteRedblock(redblockKey, userId)))
                .thenCompose(ifSuccess(v -> removeRedblockDisplay(redblock)))));
    }

    // region Helper Methods

    // region Display methods

    /**
     * Places the cube, sign and display entity of an active redblock, saves the display entity to the api, and updates
     * its Dynmap marker. Any display entities previously saved for the redblock are removed first.
     */
    private CompletableFuture<Result<GfRedblock>> createRedblockDisplay(GfRedblock redblock, List<GfUser> assignedUsers, List<String> assignedRoles) {
        var location = getLocation(redblock);
        if (location == null) return failed(RedblockMessages.ERROR_PROJECT_WORLD_NOT_LOADED.apply(redblock.getKey()));

        var woolMaterial = STATUS_PENDING.equalsIgnoreCase(redblock.getLatestStatus())
                ? Material.LIME_WOOL
                : (assignedUsers == null || assignedUsers.isEmpty()) && (assignedRoles == null || assignedRoles.isEmpty())
                        ? Material.ORANGE_WOOL
                        : Material.RED_WOOL;
        var signText = STATUS_PENDING.equalsIgnoreCase(redblock.getLatestStatus())
                ? RedblockMessages.SIGN_CLICK_TO_APPROVE_OR_DENY
                : RedblockMessages.SIGN_CLICK_THIS_IF_COMPLETED;

        return runSync(plugin, () -> {
            removeEntities(redblock.getEntities());
            createCube(woolMaterial, location, signText);
            return createDisplay(redblock.getKey(), assignedUsers, assignedRoles, location, redblock.getMessage());
        }).thenCompose(displayUuid -> redblockApiService.replaceRedblockEntities(redblock.getKey(), List.of(displayUuid)))
          .thenCompose(entitiesResult -> {
              if (entitiesResult.isFailure()) logger.warning("Failed to save the display entity of RedBlock " + redblock.getKey() + ": " + entitiesResult.getErrorMessage());
              return fetchRedblock(redblock.getKey());
          })
          .thenApply(result -> result.ifSuccess(rb -> updateDynmapMarker(rb, assignedUsers, assignedRoles)))
          .exceptionally(ex -> {
              logger.log(Level.SEVERE, "Failed to place RedBlock " + redblock.getKey() + " in the world.", ex);
              return Result.failure(RedblockMessages.ERROR_REDBLOCK_NOT_PLACED.apply(redblock.getKey()));
          });
    }

    /**
     * Removes the cube, sign, display entities and Dynmap marker of a redblock that is no longer active.
     */
    private CompletableFuture<Result<GfRedblock>> removeRedblockDisplay(GfRedblock redblock) {
        activeRedblocks.remove(redblock.getKey());
        deleteDynmapMarker(redblock.getKey());

        var location = getLocation(redblock);
        if (location == null) return failed(RedblockMessages.ERROR_PROJECT_WORLD_NOT_LOADED.apply(redblock.getKey()));

        return runSync(plugin, () -> {
            removeEntities(redblock.getEntities());
            createCube(Material.AIR, location);
            return null;
        }).thenCompose(v -> redblockApiService.clearRedblockEntities(redblock.getKey()))
          .thenApply(cleared -> {
              if (cleared.isFailure()) logger.warning("Failed to clear the display entities of RedBlock " + redblock.getKey() + ": " + cleared.getErrorMessage());
              return Result.success(redblock);
          })
          .exceptionally(ex -> {
              logger.log(Level.SEVERE, "Failed to remove RedBlock " + redblock.getKey() + " from the world.", ex);
              return Result.failure(RedblockMessages.ERROR_REDBLOCK_NOT_REMOVED.apply(redblock.getKey()));
          });
    }

    // endregion

    // region Dynmap methods

    private void updateDynmapMarker(GfRedblock redblock, List<GfUser> assignedUsers, List<String> assignedRoles) {
        if (dynmapService == null) return;
        deleteDynmapMarker(redblock.getKey());

        var location = getLocation(redblock);
        if (!redblock.isActive() || location == null) return;

        var pending = STATUS_PENDING.equalsIgnoreCase(redblock.getLatestStatus());
        var markerHtml = createMarkerHtml(redblock, pending ? STATUS_PENDING : STATUS_INCOMPLETE, assignedUsers, assignedRoles);
        if (!dynmapService.tryCreateMarker(pending ? PENDING_MARKER_SET : INCOMPLETE_MARKER_SET, redblock.getKey() + "_redblock", markerHtml, location, pending ? PENDING_MARKER_ICON : INCOMPLETE_MARKER_ICON, true))
            logger.warning("Failed to create RedBlock marker for " + redblock.getKey());
    }

    private void deleteDynmapMarker(String redblockKey) {
        if (dynmapService == null) return;
        dynmapService.tryDeleteMarker(INCOMPLETE_MARKER_SET, redblockKey + "_redblock");
        dynmapService.tryDeleteMarker(PENDING_MARKER_SET, redblockKey + "_redblock");
    }

    // endregion

    // region Cache warming methods

    /**
     * Loads every active redblock of every mapped project into the cache, and adds their Dynmap markers. Completes
     * exceptionally if any project or redblock fails to load.
     */
    private CompletableFuture<Void> loadCache() {
        logger.info("Loading RedBlocks from the api...");
        var projectLoads = configService.getMappedProjectKeys().stream()
                .map(projectKey -> loadProjectPage(projectKey, 1))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(projectLoads)
                .thenRun(() -> logger.info("Loaded " + activeRedblocks.size() + " active RedBlocks."));
    }

    private CompletableFuture<Void> loadProjectPage(String projectKey, long page) {
        var search = new GfRedblockSearchRequest();
        search.statusFilter = new GfRedblockSearchRequest.StatusFilter(List.of(STATUS_INCOMPLETE, STATUS_PENDING), GfRedblockSearchRequest.StatusFilter.MatchType.OR);
        search.deletionFilter = new GfRedblockSearchRequest.DeletionFilter(List.of(), GfRedblockSearchRequest.DeletionFilter.MatchType.AND);
        search.resultsPerPage = CACHE_LOAD_PAGE_SIZE;
        search.currentPage = page;

        return redblockApiService.searchRedblocks(projectKey, search).thenCompose(result -> {
            if (result.isFailure())
                return CompletableFuture.failedFuture(new IllegalStateException("Could not search project " + projectKey + " (page " + page + "): " + result.getErrorMessage()));

            // the search only returns the lightweight model, so fetch the full redblocks one page at a time
            var hits = result.getData().getResults();
            var fetches = hits.stream()
                    .map(hit -> fetchRedblock(hit.getKey()).thenCompose(fetched -> {
                        if (fetched.isFailure())
                            return CompletableFuture.failedFuture(new IllegalStateException("Could not load RedBlock " + hit.getKey() + ": " + fetched.getErrorMessage()));
                        var redblock = fetched.getData();
                        var roles = redblock.getRoleAssignments().stream().map(GfRedblockRoleAssignment::getRoleName).toList();
                        return resolveAssignedUsers(redblock).thenAccept(users -> updateDynmapMarker(redblock, users, roles));
                    }))
                    .toArray(CompletableFuture[]::new);

            var hasMorePages = !hits.isEmpty() && page * CACHE_LOAD_PAGE_SIZE < result.getData().getTotalResults();
            return CompletableFuture.allOf(fetches).thenCompose(v -> hasMorePages
                    ? loadProjectPage(projectKey, page + 1)
                    : CompletableFuture.completedFuture(null));
        });
    }

    // endregion

    private CompletableFuture<Result<GfRedblock>> changeStatus(String redblockKey, Player changedBy, String newStatus) {
        return fetchRedblock(redblockKey).thenCompose(ifSuccess(redblock -> resolveUserId(changedBy)
                .thenCompose(ifSuccess(userId -> redblockApiService.addRedblockStatus(redblockKey, newStatus, userId)))
                .thenCompose(ifSuccess(status -> fetchRedblock(redblockKey)))
                .thenCompose(ifSuccess(updated -> STATUS_APPROVED.equals(newStatus) ? removeRedblockDisplay(updated) : createRedblockDisplay(updated, resolveAssignedUsers(updated).join(), updated.getRoleAssignments().stream().map(GfRedblockRoleAssignment::getRoleName).toList())))));
    }

    /**
     * Fetches a redblock from the api and puts it in the cache if it is active, or evicts it if it is not.
     */
    private CompletableFuture<Result<GfRedblock>> fetchRedblock(String redblockKey) {
        return redblockApiService.getRedblock(redblockKey).thenApply(result -> result.ifSuccess(redblock -> {
            if (redblock.isActive()) activeRedblocks.put(redblock.getKey(), redblock);
            else activeRedblocks.remove(redblock.getKey());
        }));
    }

    private CompletableFuture<Result<Long>> resolveUserId(Player player) {
        var user = userService.getUserByUuid(player.getUniqueId());
        if (user != null) return CompletableFuture.completedFuture(Result.success(user.getUserId()));
        return userApiService.getUserByMinecraftUuid(player.getUniqueId()).thenApply(result -> result.map(GfUser::getUserId));
    }

    /**
     * Resolves the users assigned to a redblock, using the user cache first and the api for any that are not cached.
     * Users that cannot be resolved are left out.
     */
    private CompletableFuture<List<GfUser>> resolveAssignedUsers(GfRedblock redblock) {
        var lookups = redblock.getUserAssignments().stream()
                .map(assignment -> {
                    var cached = userService.getUserById(assignment.getAssignedTo());
                    if (cached != null) return CompletableFuture.completedFuture(Result.success(cached));
                    return userApiService.getUserById(assignment.getAssignedTo()).thenApply(result -> result.ifFailure(error ->
                            logger.warning("Failed to resolve user " + assignment.getAssignedTo() + " assigned to RedBlock " + redblock.getKey() + ": " + error)));
                })
                .toList();

        return CompletableFuture.allOf(lookups.toArray(CompletableFuture[]::new)).thenApply(v -> lookups.stream()
                .map(CompletableFuture::join)
                .filter(Result::isSuccess)
                .map(Result::getData)
                .toList());
    }

    private @Nullable Location getLocation(GfRedblock redblock) {
        var world = configService.getWorld(configService.getProjectKeyOf(redblock.getKey()));
        return world == null ? null : new Location(world, redblock.getX(), redblock.getY(), redblock.getZ());
    }

    // endregion
}
