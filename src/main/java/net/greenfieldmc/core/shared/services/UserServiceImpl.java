package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserServiceImpl implements IUserService {

    private final IGreenfieldCorePlugin plugin;
    private final IGreenfieldUserApiService userApi;

    private final Map<Long, GfUser> userIdCache = new HashMap<>();
    private final Map<UUID, Long> userUuidToIdCache = new HashMap<>();

    private final Set<Long> userIdsBeingFetched = new HashSet<>();
    private final Set<UUID> userUuidsBeingFetched = new HashSet<>();
    private final Set<String> usernamesBeingFetched = new HashSet<>();
    private boolean isRefreshingCache = false;

    public UserServiceImpl(IGreenfieldCorePlugin plugin, IGreenfieldUserApiService userApiService) {
        this.plugin = plugin;
        this.userApi = userApiService;
        refreshCache();
    }

    @Override
    public GfUser getUserById(long userId) {
        if (userIdsBeingFetched.contains(userId)) return null;

        var cacheHit = userIdCache.get(userId);
        if (cacheHit != null) return cacheHit;

        userIdsBeingFetched.add(userId);
        userApi.getUserById(userId)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        var user = result.getData();
                        userIdCache.put(userId, user);
                        userUuidToIdCache.put(user.getMinecraftUuid(), userId);
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to fetch user by ID " + userId + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((result, ex) -> userIdsBeingFetched.remove(userId));

        return null;
    }

    @Override
    public GfUser getUserByUuid(UUID userUid) {
        if (userUuidsBeingFetched.contains(userUid)) return null;

        var userId = userUuidToIdCache.get(userUid);
        if (userId != null) {
            var cacheHit = userIdCache.get(userId);
            if (cacheHit != null) return cacheHit;
        }

        userUuidsBeingFetched.add(userUid);
        userApi.getUserByMinecraftUuid(userUid)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        var user = result.getData();
                        userIdCache.put(user.getUserId(), user);
                        userUuidToIdCache.put(userUid, user.getUserId());
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to fetch user by UUID " + userUid + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((result, ex) -> userUuidsBeingFetched.remove(userUid));

        return null;
    }

    @Override
    public GfUser getUserByUsername(String username) {
        if (usernamesBeingFetched.contains(username)) return null;

        // Check if we have a cached user with this username
        for (var user : userIdCache.values()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }

        usernamesBeingFetched.add(username);
        userApi.getUserByUsername(username)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        var user = result.getData();
                        userIdCache.put(user.getUserId(), user);
                        userUuidToIdCache.put(user.getMinecraftUuid(), user.getUserId());
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to fetch user by username " + username + ": " + ex.getMessage());
                    return null;
                })
                .whenComplete((result, ex) -> usernamesBeingFetched.remove(username));

        return null;
    }

    @Override
    public List<GfUser> getUsers() {
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
        userApi.getAllUsers()
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        var users = result.getData();
                        for (var user : users) {
                            userIdCache.put(user.getUserId(), user);
                            userUuidToIdCache.put(user.getMinecraftUuid(), user.getUserId());
                        }
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to refresh user cache: " + ex.getMessage());
                    return null;
                })
                .whenComplete((result, ex) -> isRefreshingCache = false);
    }
}
