package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;
import net.greenfieldmc.core.shared.utils.BoundedLruSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class UserServiceImpl implements IUserService {

    private final IGreenfieldUserApiService userApi;

    private final Map<Long, GfUser> userIdCache = new HashMap<>();
    private final Map<UUID, Long> userUuidToIdCache = new HashMap<>();

    private final Set<Long> userIdsBeingFetched = new HashSet<>();
    private final Set<UUID> userUuidsBeingFetched = new HashSet<>();
    private final Set<String> usernamesBeingFetched = new HashSet<>();
    private boolean isRefreshingCache = false;
    private final Logger logger;

    private GfUser systemUser;
    private boolean isFetchingSystemUser = false;

    // lookups the api said do not exist, so they are not requested again. Holds userIds (Long), UUIDs and usernames
    // (String, lowercase since username lookups ignore case), which never collide since they are different types
    private final BoundedLruSet<Object> knownMisses = new BoundedLruSet<>(150);

    public UserServiceImpl(IGreenfieldUserApiService userApiService, Logger logger) {
        this.userApi = userApiService;
        this.logger = logger;
        refreshCache();
        getSystemUser();
    }

    @Override
    public void updateUserCache(GfUser user) {
        if (user.isSystemUser()) {
            systemUser = user;
        }
        userIdCache.put(user.getUserId(), user);
        userUuidToIdCache.put(user.getMinecraftUuid(), user.getUserId());

        // the user exists now, so none of the ways to look them up are misses anymore
        knownMisses.remove(user.getUserId());
        knownMisses.remove(user.getMinecraftUuid());
        knownMisses.remove(user.getUsername().toLowerCase());
    }

    @Override
    public GfUser getUserById(long userId) {
        if (userIdsBeingFetched.contains(userId)) return null;

        var cacheHit = userIdCache.get(userId);
        if (cacheHit != null) return cacheHit;
        if (knownMisses.contains(userId)) return null;

        userIdsBeingFetched.add(userId);
        userApi.getUserById(userId)
                .thenAccept(result -> {
                    result.ifSuccess(this::updateUserCache);
                    if (result.isNotFound()) knownMisses.add(userId);
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to fetch user by ID " + userId + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((_, _) -> userIdsBeingFetched.remove(userId));

        return null;
    }

    @Override
    public GfUser getUserByIdCached(long userId) {
        return userIdCache.get(userId);
    }

    @Override
    public GfUser getUserByUuid(UUID userUid) {
        if (userUuidsBeingFetched.contains(userUid)) return null;

        var cacheHit = getUserByUuidCached(userUid);
        if (cacheHit != null) return cacheHit;
        if (knownMisses.contains(userUid)) return null;

        userUuidsBeingFetched.add(userUid);
        userApi.getUserByMinecraftUuid(userUid)
                .thenAccept(result -> {
                    result.ifSuccess(this::updateUserCache);
                    if (result.isNotFound()) knownMisses.add(userUid);
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to fetch user by UUID " + userUid + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((_, _) -> userUuidsBeingFetched.remove(userUid));

        return null;
    }

    @Override
    public GfUser getUserByUuidCached(UUID userUid) {
        var userId = userUuidToIdCache.get(userUid);
        if (userId != null) return userIdCache.get(userId);
        return null;
    }

    @Override
    public GfUser getUserByUsername(String username) {
        if (usernamesBeingFetched.contains(username)) return null;

        var cacheHit = getUserByUsernameCached(username);
        if (cacheHit != null) return cacheHit;
        if (knownMisses.contains(username.toLowerCase())) return null;

        usernamesBeingFetched.add(username);
        userApi.getUserByUsername(username)
                .thenAccept(result -> {
                    result.ifSuccess(this::updateUserCache);
                    if (result.isNotFound()) knownMisses.add(username.toLowerCase());
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to fetch user by username " + username + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((_, _) -> usernamesBeingFetched.remove(username));

        return null;
    }

    @Override
    public GfUser getUserByUsernameCached(String username) {
        for (var user : userIdCache.values()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public GfUser getSystemUser() {
        if (systemUser != null) return systemUser;
        if (isFetchingSystemUser) return null;

        isFetchingSystemUser = true;
        userApi.getSystemUser()
                .thenAccept(result -> {
                    if (result.isSuccess()) systemUser = result.getData();
                    else logger.warning("Failed to fetch system user: " + result.getErrorMessage());
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to fetch system user: " + ex.getMessage());
                    return null;
                })
                .whenComplete((_, _) -> isFetchingSystemUser = false);

        return null;
    }

    @Override
    public List<GfUser> getUsersCached() {
        if (isRefreshingCache) return List.copyOf(userIdCache.values());
        if (userIdCache.isEmpty()) {
            refreshCache();
        }
        return List.copyOf(userIdCache.values());
    }

    @Override
    public void refreshCache() {
        if (isRefreshingCache) return;
        isRefreshingCache = true;
        knownMisses.clear();
        logger.info("Refreshing user cache...");
        userApi.getAllUsers()
                .thenAccept(result -> result.ifSuccess(users -> users.forEach(this::updateUserCache)))
                .exceptionally(ex -> {
                    logger.warning("Failed to refresh user cache: " + ex.getMessage());
                    return null;
                })
                .whenComplete((_, _) -> isRefreshingCache = false);
    }
}
