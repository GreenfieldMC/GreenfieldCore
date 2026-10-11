package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IRedblockServiceV2 {


    /**
     * Get a redblock by its PROJ-keyId. Null if this redblock was not found in memory.
     * @param redblockKey The PROJECT-KEY of the redblock
     * @return The redblock, if found.
     */
    CompletableFuture<Result<GfRedblock>> getRedblock(String redblockKey);

    /**
     * Get all redblocks that are currently in memory. If there is none in memory, it will query the api
     * @return A list of all redblocks in memory.
     */
    CompletableFuture<Result<List<GfRedblock>>> getRedblocks();

    /**
     * Get all redblocks that are currently in memory that match the search request. If there is none in memory, it will query the api
     * @param searchRequest The search request containing the filters to apply
     * @return A list of all redblocks that match the search request
     */
    CompletableFuture<Result<List<GfRedblock>>> searchRedblocks(GfRedblockSearchRequest searchRequest);

    /**
     * Creates a new redblock
     * @param message The message of the redblock
     * @param createdBy The player who created the redblock
     * @param location The location of the redblock
     * @param userAssignments The users assigned to the redblock
     * @param roleAssignments The roles assigned to the redblock
     * @return The created redblock

     */
    CompletableFuture<Result<GfRedblock>> createRedblock(String message, Player createdBy, Location location, @Nullable List<GfUser> userAssignments, @Nullable List<String> roleAssignments);

    /**
     * Edits a redblock
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param message The new message of the redblock
     * @param userAssignments The new users assigned to the redblock
     * @param roleAssignments The new roles assigned to the redblock
     * @param editedBy The player who edited the redblock
     * @return The edited redblock
     */
    CompletableFuture<Result<GfRedblock>> editRedblock(String redblockKey, @Nullable String message, @Nullable List<GfUser> userAssignments, @Nullable List<String> roleAssignments, Player editedBy);

    /**
     * Completes a redblock
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param completedBy The player who completed the redblock
     * @return The completed redblock
     */
    CompletableFuture<Result<GfRedblock>> completeRedblock(String redblockKey, Player completedBy);

    /**
     * Denies a redblock
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param deniedBy The player who denied the redblock
     * @return The denied redblock
     */
    CompletableFuture<Result<GfRedblock>> denyRedblock(String redblockKey, Player deniedBy);

    /**
     * Deletes a redblock
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param deletedBy The player who deleted the redblock
     * @return The deleted redblock
     */
    CompletableFuture<Result<GfRedblock>> deleteRedblock(String redblockKey, Player deletedBy);

}
