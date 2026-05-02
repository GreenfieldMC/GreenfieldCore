package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.greenfieldapi.models.users.GfDiscordConnection;
import net.greenfieldmc.core.greenfieldapi.models.users.GfPatreonConnection;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.greenfieldapi.models.Result;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IGreenfieldUserApiService extends IModuleService<IGreenfieldUserApiService> {

    /**
     * Gets a user by their Minecraft UUID.
     * @param minecraftUuid The Minecraft UUID of the user
     * @return A CompletableFuture containing a Result with the user data or error message
     */
    CompletableFuture<Result<GfUser>> getUserByMinecraftUuid(UUID minecraftUuid);

    /**
     * Gets a user by their internal user ID.
     * @param userId The internal user ID
     * @return A CompletableFuture containing a Result with the user data or error message
     */
    CompletableFuture<Result<GfUser>> getUserById(long userId);

    /**
     * Creates or updates a user by their Minecraft UUID (PUT operation).
     * @param minecraftUuid The Minecraft UUID of the user
     * @param username The username data to create or update
     * @return A CompletableFuture containing a Result with the updated user data or error message
     */
    CompletableFuture<Result<GfUser>> createUser(UUID minecraftUuid, String username);

    /**
     * Updates an existing user by their Minecraft UUID (PATCH operation).
     *
     * @param minecraftUuid The Minecraft UUID of the user
     * @param username The user data to update (partial update)
     * @return A CompletableFuture containing a Result with the updated user data or error message
     */
    CompletableFuture<Result<GfUser>> updateUser(UUID minecraftUuid, String username);

    /**
     * Gets the Discord connection for a user.
     *
     * @param userId The internal user ID
     * @return A CompletableFuture containing a Result with the Discord connection data or error message
     */
    CompletableFuture<Result<GfDiscordConnection[]>> getDiscordConnection(long userId);

    /**
     * Gets the Patreon connection for a user.
     *
     * @param userId The internal user ID
     * @return A CompletableFuture containing a Result with the Patreon connection data or error message
     */
    CompletableFuture<Result<GfPatreonConnection[]>> getPatreonConnection(long userId);

    /**
     * Gets the Discord connection link for a user to connect their account.
     * @param userId The internal user ID
     * @return A CompletableFuture containing a Result with the Discord connection link or error message
     */
    CompletableFuture<Result<String>> getDiscordConnectionLink(long userId);

    /**
     * This refreshes the patreon connection data for the specified connection ID
     * @param patreonConnectionId The ID of the patreon connection to refresh
     * @return A CompletableFuture containing a Result with the updated Patreon connection data or error message
     */
    CompletableFuture<Result<GfPatreonConnection>> refreshPatreonConnection(long patreonConnectionId);
}
