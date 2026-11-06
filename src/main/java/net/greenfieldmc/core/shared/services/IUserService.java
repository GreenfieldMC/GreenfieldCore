package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;

import java.util.List;
import java.util.UUID;

/**
 * Service for gathering user information from the GreenfieldCoreApi. It is built on top of a caching system, so it will first check the cache for a user before querying the api unless the method explicitly states it will only check the cache. If a user is not found in the cache, it will query the api and cache the result for future use.
 */
public interface IUserService {

    /**
     * Updates the user cache with the given user. If the user is already in the cache, it will be updated, otherwise it will be added to the cache.
     * @param user The user to update or add to the cache
     */
    void updateUserCache(GfUser user);

    /**
     * Attempt to find a user by their internal GreenfieldCoreApi userId. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param userId The internal GreenfieldCoreApi userId
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserById(long userId);

    /**
     * Attempt to find a user by their internal GreenfieldCoreApi userId. This will only check the cache and will not attempt to query the api for the user if they are not found in memory. If the user is not found in memory, this will return null.
     * @param userId The internal GreenfieldCoreApi userId
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByIdCached(long userId);

    /**
     * Attempt to find a user by their uuid. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param userUid The uuid of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUuid(UUID userUid);

    /**
     * Attempt to find a user by their uuid. This will only check the cache and will not attempt to query the api for the user if they are not found in memory. If the user is not found in memory, this will return null.
     * @param userUid The uuid of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUuidCached(UUID userUid);

    /**
     * Attempt to find a user by their username. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param username The username of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUsername(String username);

    /**
     * Attempt to find a user by their username. This will only check the cache and will not attempt to query the api for the user if they are not found in memory. If the user is not found in memory, this will return null.
     * @param username The username of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUsernameCached(String username);

    /**
     * Attempt to get the system user, which is used for actions performed by the system rather than a specific user. The system user is fetched when this service is created, so this will usually return immediately. If the system user is not in memory yet, this will return null and attempt to find the system user in the api to cache it for later use.
     * @return The system GfUser if found, otherwise null
     */
    GfUser getSystemUser();

    /**
     * Get the list of currently cached users.
     * @return The list of currently cached users
     */
    List<GfUser> getUsersCached();

    /**
     * This will clear the user cache and attempt to repopulate it with the users from the api. This is useful if you know there have been changes to the users in the api and want to ensure you have the most up-to-date information in memory.
     */
    void refreshCache();

}
