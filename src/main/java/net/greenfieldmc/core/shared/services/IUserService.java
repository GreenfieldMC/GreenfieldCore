package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;

import java.util.List;
import java.util.UUID;

public interface IUserService extends IModuleService<IUserService> {

    /**
     * Attempt to find a user by their internal GreenfieldCoreApi userId. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param userId The internal GreenfieldCoreApi userId
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserById(long userId);

    /**
     * Attempt to find a user by their uuid. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param userUid The uuid of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUuid(UUID userUid);

    /**
     * Attempt to find a user by their username. If the user is not in memory yet, this will return null and attempt to find the user in the api to cache them for later use. If the user is not found in the api, this will return null.
     * @param username The username of the user
     * @return The GfUser if found, otherwise null
     */
    GfUser getUserByUsername(String username);

    /**
     * Get the list of currently cached users.
     * @return The list of currently cached users
     */
    List<GfUser> getUsers();

    /**
     * This will clear the user cache and attempt to repopulate it with the users from the api. This is useful if you know there have been changes to the users in the api and want to ensure you have the most up-to-date information in memory.
     */
    void refreshCache();

}
