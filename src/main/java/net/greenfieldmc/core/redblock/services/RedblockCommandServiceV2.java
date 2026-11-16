package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.command.brigadier.CommandInputMode;
import com.njdaeger.pdk.command.brigadier.IAsyncCommandContext;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.redblock.RedblockConstants;
import net.greenfieldmc.core.CommonMessages;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.redblock.arguments.ProjectArgument;
import net.greenfieldmc.core.redblock.arguments.RedblockArgumentV2;
import net.greenfieldmc.core.redblock.arguments.RoleListArgument;
import net.greenfieldmc.core.redblock.arguments.StatusListArgument;
import net.greenfieldmc.core.redblock.arguments.UserListArgument;
import net.greenfieldmc.core.redblock.paginators.RedblockInfoPaginatorV2;
import net.greenfieldmc.core.redblock.paginators.RedblockListPaginatorV2;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import net.greenfieldmc.core.shared.services.IUserService;
import net.greenfieldmc.core.shared.services.IVaultService;
import com.njdaeger.pdk.serviceprovider.Nullable;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedblockCommandServiceV2 {

    private final IRedblockServiceV2 redblockService;
    private final IUserService userService;
    private final @Nullable IVaultService vaultService;
    private final IRedblockConfigService configService;
    private final @Nullable IEssentialsService essentialsService;

    private final ChatPaginator<GfRedblock.GfRedblockInfo, Pair<ICommandContext, IUserService>> infoPaginator;
    private final ChatPaginator<GfRedblock, Pair<ICommandContext, IUserService>> listPaginator;

    public RedblockCommandServiceV2(IGreenfieldCorePlugin plugin, IRedblockServiceV2 redblockService, IRedblockConfigService configService, IUserService userService, @Nullable IVaultService vaultService, @Nullable IEssentialsService essentialsService) {
        this.redblockService = redblockService;
        this.configService = configService;
        this.userService = userService;
        this.vaultService = vaultService;
        this.essentialsService = essentialsService;

        this.listPaginator = new RedblockListPaginatorV2(configService, userService).build();
        this.infoPaginator = new RedblockInfoPaginatorV2().build();

        registerCommands(plugin);
    }

    private void registerCommands(IGreenfieldCorePlugin plugin) {
        CommandBuilder.ofAsync("rbcreate", "rbc")
                .description("Create a new RedBlock")
                .permission("greenfieldcore.redblock.create")
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Enter a description for the RedBlock"))
                .executes(this::create)
                .flag("assign", "Assign this RedBlock to a group of users", new UserListArgument(userService, false))
                .flag("role", "Assign this RedBlock to a group of roles", new RoleListArgument(vaultService, false))
                .register(plugin);

        CommandBuilder.ofAsync("rbdelete", "rbr", "rbremove", "rbrem", "rbdel")
                .description("Delete an incomplete or pending RedBlock")
                .permission("greenfieldcore.redblock.delete")
                .flag("key", "The RedBlock to delete", new RedblockArgumentV2(redblockService, rb -> rb.isIncomplete() || rb.isPending()))
                .canExecute(this::delete)
                .register(plugin);

        CommandBuilder.ofAsync("rbapprove", "rba")
                .description("Approve a pending RedBlock")
                .permission("greenfieldcore.redblock.approve")
                .flag("key", "The RedBlock to approve", new RedblockArgumentV2(redblockService, GfRedblock::isPending))
                .canExecute(this::approve)
                .register(plugin);

        CommandBuilder.ofAsync("rbdeny", "rbd")
                .description("Deny a pending RedBlock")
                .permission("greenfieldcore.redblock.deny")
                .flag("key", "The RedBlock to deny", new RedblockArgumentV2(redblockService, GfRedblock::isPending))
                .canExecute(this::deny)
                .register(plugin);

        CommandBuilder.ofAsync("rbcomplete", "rbcmp")
                .description("Mark an incomplete RedBlock as completed")
                .permission("greenfieldcore.redblock.complete")
                .flag("key", "The RedBlock to complete", new RedblockArgumentV2(redblockService, GfRedblock::isIncomplete))
                .canExecute(this::complete)
                .register(plugin);

        CommandBuilder.ofAsync("rbtp", "rbgoto", "rbg")
                .description("Teleport to an incomplete RedBlock")
                .permission("greenfieldcore.redblock.goto")
                .flag("key", "The RedBlock to teleport to", new RedblockArgumentV2(redblockService, rb -> rb.isIncomplete() || rb.isPending()))
                .flag("mine", "Teleport to the nearest incomplete RedBlock assigned to you")
                .flag("nearest", "Teleport to the nearest incomplete RedBlock")
                .exclusiveFlags("key", "nearest")
                .exclusiveFlags("key", "mine")
                .canExecute(this::teleport)
                .register(plugin);

        CommandBuilder.ofAsync("rbedit", "rbe")
                .description("Edit an incomplete RedBlock")
                .permission("greenfieldcore.redblock.edit")
                .flag("key", "The RedBlock to edit", new RedblockArgumentV2(redblockService, GfRedblock::isIncomplete))
                .flag("assign", "Assign this RedBlock to a group of users", new UserListArgument(userService, false))
                .flag("unassign", "Unassign this RedBlock from a group of users", new UserListArgument(userService, true))
                .flag("role", "Assign this RedBlock to a group of roles", new RoleListArgument(vaultService, false))
                .flag("unrole", "Unassign this RedBlock from a group of roles", new RoleListArgument(vaultService, true))
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Enter a new description for the RedBlock")).executes(this::edit)
                .canExecute(this::edit)
                .register(plugin);

        CommandBuilder.ofAsync("rbinfo", "rbi")
                .description("View information about a RedBlock")
                .permission("greenfieldcore.redblock.info")
                .flag("key", "The RedBlock to view", new RedblockArgumentV2(redblockService, rb -> true))
                .flag("page", "The page of information to display", PdkArgumentTypes.integer(1, 1000))
                .canExecute(this::info)
                .register(plugin);

        CommandBuilder.ofAsync("rbl", "rblist")
                .description("List RedBlocks")
                .permission("greenfieldcore.redblock.list")
                .inputMode(CommandInputMode.GREEDY)
                .then("project", new ProjectArgument(configService, redblockService)).executes(this::list)
                .flag("mine", "List only RedBlocks assigned to you")
                .flag("unassigned", "List only unassigned RedBlocks")
                // each group sets the same search filter, so only one flag of a group can be used
                .exclusiveFlags("mine", "unassigned", "assignedTo", "assignedToExact", "assignedToAll")
                .exclusiveFlags("role", "roleExact", "roleAll")
                .exclusiveFlags("unassigned", "role", "roleExact", "roleAll")
                .flag("status", "List only RedBlocks with any of these statuses (incomplete, pending, approved, deleted)", new StatusListArgument())
                .flag("assignedTo", "List only RedBlocks assigned to any of the users", new UserListArgument(userService, false))
                .flag("assignedToExact", "List only RedBlocks assigned to exactly these users", new UserListArgument(userService, false))
                .flag("assignedToAll", "List only RedBlocks assigned to all of the users", new UserListArgument(userService, false))
//                .flag("createdBy", "List only RedBlocks created by a user", new UserListArgument(userService, false))
//                .flag("completedBy", "List only RedBlocks completed by a user", new UserListArgument(userService, false))
//                .flag("approvedBy", "List only RedBlocks approved by a user", new UserListArgument(userService, false))
                .flag("role", "List only RedBlocks assigned any of the roles", new RoleListArgument(vaultService, false))
                .flag("roleExact", "List only RedBlocks assigned exactly these roles", new RoleListArgument(vaultService, false))
                .flag("roleAll", "List only RedBlocks assigned all of the roles", new RoleListArgument(vaultService, false))
                .flag("radius", "List only RedBlocks within a radius of you", PdkArgumentTypes.longArg(1, 1000))
                .flag("page", "The page of results to display", PdkArgumentTypes.integer(1, 1000))
                .canExecute(this::list)
                .register(plugin);

    }

    // create a new redblock
    private void create(IAsyncCommandContext ctx) throws PDKCommandException {
        var assignUserList = ctx.<List<GfUser>>getFlag("assign");
        var assignRoleList = ctx.<List<String>>getFlag("role");
        var description = ctx.getTyped("description", String.class);

        var rbResult = redblockService.createRedblock(description, ctx.asPlayer(), ctx.getLocation(), assignUserList, assignRoleList).join();
        if (rbResult.isFailure()) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_GENERATED.apply(rbResult.getErrorMessage()));
        ctx.send(RedblockMessages.REDBLOCK_CREATED_NEW.apply(rbResult.getData().getKey()));
    }

    // delete the redblock
    private void delete(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_INCOMPLETE, RedblockConstants.STATUS_PENDING), RedblockConstants.STATUS_PENDING + " or " + RedblockConstants.STATUS_INCOMPLETE, 10L);
        var deleteResult = redblockService.deleteRedblock(rbKey, ctx.asPlayer()).join();
        if (deleteResult.isFailure()) ctx.error(deleteResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_DELETED_NEW.apply(rbKey));
    }

    // mark the redblock as approved
    private void approve(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_PENDING), RedblockConstants.STATUS_PENDING, 10L);
        var approveResult = redblockService.approveRedblock(rbKey, ctx.asPlayer()).join();
        if (approveResult.isFailure()) ctx.error(approveResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_APPROVED_NEW.apply(rbKey));
    }

    // mark the redblock as incomplete
    private void deny(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_PENDING), RedblockConstants.STATUS_PENDING, 10L);
        var denyResult = redblockService.denyRedblock(rbKey, ctx.asPlayer()).join();
        if (denyResult.isFailure()) ctx.error(denyResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_DENIED_NEW.apply(rbKey));
    }

    // mark the redblock as completed
    private void complete(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_INCOMPLETE), RedblockConstants.STATUS_INCOMPLETE, 10L);
        var completeResult = redblockService.completeRedblock(rbKey, ctx.asPlayer()).join();
        if (completeResult.isFailure()) ctx.error(completeResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_COMPLETED_NEW.apply(rbKey));
    }

    private void teleport(IAsyncCommandContext ctx) throws PDKCommandException {
        var goToNearest = ctx.hasFlag("nearest");
        if (goToNearest && ctx.hasFlag("key")) ctx.error(RedblockMessages.ERROR_GOTO_CONFLICTING_FLAGS);

        // -mine limits the nearest search to redblocks assigned to the player
        var userFilter = ctx.hasFlag("mine")
                ? GfRedblockSearchRequest.UserAssignmentFilter.assignedToAny(List.of(resolveUser(ctx).getUserId()))
                : null;
        var typeMessage = ctx.hasFlag("mine") ? RedblockConstants.STATUS_INCOMPLETE + " (assigned to you)" : RedblockConstants.STATUS_INCOMPLETE;
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_INCOMPLETE), userFilter, typeMessage, null);

        var redblockResult = redblockService.getRedblock(rbKey).join();
        if (redblockResult.isFailure()) ctx.error(redblockResult.getErrorMessage());
        var redblock = redblockResult.getData();

        var playerLocation = ctx.getLocation();
        var redblockLocation = redblock.getVector().toLocation(playerLocation.getWorld());

        if (essentialsService != null) essentialsService.setUserLastLocation(ctx.asPlayer(), playerLocation);

        ctx.sync(() -> {
            try {
                ctx.asPlayer().teleport(redblockLocation);
            } catch (CommandSenderTypeException e) {
                throw new RuntimeException(e);
            }
        });
        ctx.send(RedblockMessages.REDBLOCK_GOTO_NEW.apply(redblock.getKey()));
    }

    private void edit(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_INCOMPLETE), RedblockConstants.STATUS_INCOMPLETE, 10L);
        var editingUser = resolveUser(ctx);

        var description = ctx.getTyped("description", String.class, null);
        var userAdditions = ctx.<List<GfUser>>getFlag("assign");
        var userRemovals = ctx.<List<GfUser>>getFlag("unassign");
        var clearUsers = userRemovals != null && userRemovals.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase("*"));
        var roleAdditions = ctx.<List<String>>getFlag("role");
        var roleRemovals = ctx.<List<String>>getFlag("unrole");
        var clearRoles = roleRemovals != null && roleRemovals.stream().anyMatch(r -> r.equalsIgnoreCase("*"));

        var result = redblockService.editRedblock(rbKey, description, userAdditions, clearUsers ? List.of() : userRemovals, roleAdditions, clearRoles ? List.of() : roleRemovals, editingUser).join();
        if (result.isFailure())
            ctx.error(RedblockMessages.ERROR_EDIT_PARTIALLY_SAVED.apply(rbKey, List.of(result.getErrorMessage())));

        ctx.send(RedblockMessages.REDBLOCK_EDITED_NEW.apply(rbKey));
    }

    // show the details of the redblock from -key, or else the nearest incomplete or pending one
    private void info(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, GfRedblockSearchRequest.StatusFilter.anyOf(RedblockConstants.STATUS_INCOMPLETE, RedblockConstants.STATUS_PENDING), RedblockConstants.STATUS_INCOMPLETE + " or " + RedblockConstants.STATUS_PENDING, 10L);
        var redblockResult = redblockService.getRedblock(rbKey).join();
        if (redblockResult.isFailure()) ctx.error(redblockResult.getErrorMessage());
        var redblock = redblockResult.getData();

        // only show distance when the sender is in the world of the redblock's project
        var senderLocation = ctx.getLocationOrNull();
        var distanceFrom = senderLocation != null && redblock.getProject() != null
                && redblock.getProject().getProjectKey().equals(configService.getProjectKey(senderLocation.getWorld()))
                ? senderLocation
                : null;

        var page = ctx.<Integer>getFlag("page", 1);
        //noinspection DataFlowIssue
        infoPaginator.generatePage(Pair.of(ctx, userService), redblock.getRedblockInfo(distanceFrom, userService), page)
                .sendTo(RedblockMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void list(IAsyncCommandContext ctx) throws PDKCommandException {
        if (!ctx.isLocatable() && !ctx.hasTyped("project")) {
            ctx.error("You must select a project to look for RedBlocks in. Available Projects: " + String.join(", ", configService.getMappedProjectKeys()));
        }

        var projectWorld = ctx.getTyped("project", World.class, ctx.getLocation().getWorld());

        var statuses = ctx.<List<String>>getFlag("status", List.of());
        var unassigned = ctx.hasFlag("unassigned");
        var mine = ctx.hasFlag("mine");

        var assignedToAny = ctx.<List<GfUser>>getFlag("assignedTo");
        var assignedToExactly = ctx.<List<GfUser>>getFlag("assignedToExact");
        var assignedToAll = ctx.<List<GfUser>>getFlag("assignedToAll");

        var roleAny = ctx.<List<String>>getFlag("role");
        var roleExactly = ctx.<List<String>>getFlag("roleExact");
        var roleAll = ctx.<List<String>>getFlag("roleAll");

//        var createdByOr = ctx.<List<GfUser>>getFlag("createdBy"
        var radius = ctx.<Long>getFlag("radius", -1L);
        var page = ctx.<Integer>getFlag("page", 1);

        var search = new GfRedblockSearchRequest();
        search.location = ctx.isLocatable() ? GfRedblockSearchRequest.Location.fromBukkit(ctx.getLocation()) : null;
        //noinspection DataFlowIssue
        search.radius = radius == -1L ? null : radius;
        search.resultsPerPage = 8;
        //noinspection DataFlowIssue
        search.currentPage = page;

        var filterGroup = new GfRedblockSearchRequest.FilterGroup();

        // the status flag is treated as one state per redblock: "deleted" if it is deleted, otherwise its current status.
        // listing deleted alongside real statuses needs an OR, so it gets its own filter group
        var statusList = statuses.stream().filter(RedblockConstants.STATUSES::contains).distinct().toList();
        if (!statusList.isEmpty()) {
            filterGroup
                    .status(GfRedblockSearchRequest.StatusFilter.anyOf(statusList))
                    .deletion(GfRedblockSearchRequest.DeletionFilter.notDeleted());
        }
        if (statuses.contains(StatusListArgument.DELETED)) {
            filterGroup
                    .deletion(GfRedblockSearchRequest.DeletionFilter.deletedOnly());
        }

        if (unassigned) filterGroup.userAssignment(GfRedblockSearchRequest.UserAssignmentFilter.unassigned());
        if (mine) {
            var user = resolveUser(ctx);
            filterGroup.userAssignment(GfRedblockSearchRequest.UserAssignmentFilter.assignedToAny(List.of(user.getUserId())));
        }
        if (assignedToAny != null && !assignedToAny.isEmpty()) {
            var userIds = assignedToAny.stream().map(GfUser::getUserId).toList();
            filterGroup.userAssignment(GfRedblockSearchRequest.UserAssignmentFilter.assignedToAny(userIds));
        }
        if (assignedToExactly != null && !assignedToExactly.isEmpty()) {
            var userIds = assignedToExactly.stream().map(GfUser::getUserId).toList();
            filterGroup.userAssignment(GfRedblockSearchRequest.UserAssignmentFilter.assignedToExactly(userIds));
        }
        if (assignedToAll != null && !assignedToAll.isEmpty()) {
            var userIds = assignedToAll.stream().map(GfUser::getUserId).toList();
            filterGroup.userAssignment(GfRedblockSearchRequest.UserAssignmentFilter.assignedToAll(userIds));
        }

        if (roleAny != null && !roleAny.isEmpty()) filterGroup.roleAssignment(GfRedblockSearchRequest.RoleAssignmentFilter.assignedToAny(roleAny));
        if (roleExactly != null && !roleExactly.isEmpty()) filterGroup.roleAssignment(GfRedblockSearchRequest.RoleAssignmentFilter.assignedToExactly(roleExactly));
        if (roleAll != null && !roleAll.isEmpty()) filterGroup.roleAssignment(GfRedblockSearchRequest.RoleAssignmentFilter.assignedToAll(roleAll));

        if (!filterGroup.isEmpty()) search.addFilterGroup(filterGroup);

        var searchResult = redblockService.searchRedblocks(projectWorld, search).join();
        if (searchResult.isFailure()) ctx.error(searchResult.getErrorMessage());

        var displayedResults = searchResult.getData().getResults().parallelStream()
                        .map(srb -> redblockService.getRedblock(srb.getKey()).join())
                        .filter(Result::isSuccess)
                        .map(Result::getData)
                        .toList();

        listPaginator.generatePage(Pair.of(ctx, userService), displayedResults, page, (int) searchResult.getData().getTotalResults())
                .sendTo(RedblockMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());

    }

    @NotNull
    private GfUser resolveUser(ICommandContext ctx) throws PDKCommandException {
        var user = !ctx.isPlayer()
                ? userService.getSystemUser()
                : userService.getUserByUuid(ctx.asPlayer().getUniqueId());

        if (user == null) ctx.error(CommonMessages.ERROR_USER_NOT_FOUND.apply(!ctx.isPlayer() ? "System" : ctx.asPlayer().getName()));
        return user;
    }

    @NotNull
    private String resolveRedblockKey(ICommandContext ctx, GfRedblockSearchRequest.StatusFilter statusFilter, String typeMessage, @Nullable Long radius) throws PDKCommandException {
        return resolveRedblockKey(ctx, statusFilter, null, typeMessage, radius);
    }

    /**
     * Gets the redblock from the -key flag, or else the nearest non-deleted redblock matching the filters.
     * @param userFilter If set, the nearest search only considers redblocks matching this user assignment filter
     * @param radius The search radius, or null to search the whole project
     */
    @NotNull
    private String resolveRedblockKey(ICommandContext ctx, GfRedblockSearchRequest.StatusFilter statusFilter, @Nullable GfRedblockSearchRequest.UserAssignmentFilter userFilter, String typeMessage, @Nullable Long radius) throws PDKCommandException {
        var rbFlag = ctx.<GfRedblock>getFlag("key");
        if (rbFlag == null && ctx.hasFlag("key"))
            ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_KEY.apply(typeMessage, ctx.getRawFlag("key")));
        if (rbFlag != null) return rbFlag.getKey();

        var search = new GfRedblockSearchRequest();
        search.location = GfRedblockSearchRequest.Location.fromBukkit(ctx.getLocation());
        search.radius = radius;
        search.resultsPerPage = 1;
        search.addFilterGroup(new GfRedblockSearchRequest.FilterGroup()
                .status(statusFilter)
                .deletion(GfRedblockSearchRequest.DeletionFilter.notDeleted())
                .userAssignment(userFilter));

        var searchResult = redblockService.searchRedblocks(ctx.getLocation().getWorld(), search).join();
        if (searchResult.isFailure() || searchResult.getData().getResults().isEmpty())
            ctx.error(radius == null
                    ? RedblockMessages.ERROR_REDBLOCK_NOT_FOUND.apply(typeMessage)
                    : RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_RADIUS.apply(typeMessage, Math.toIntExact(radius)));

        return searchResult.getData().getResults().getFirst().getKey();
    }

}
