package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockSearchResult;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IRedblockServiceV2 {

    String STATUS_INCOMPLETE = "Incomplete";
    String STATUS_PENDING = "Pending";
    String STATUS_APPROVED = "Approved";

    /**
     * Get a redblock by its PROJ-keyId. If the redblock is in memory, the cached copy is returned and refreshed in the
     * background, otherwise it is fetched from the api.
     * @param redblockKey The PROJECT-KEY of the redblock
     * @return The redblock, if found.
     */
    CompletableFuture<Result<GfRedblock>> getRedblock(String redblockKey);

    /**
     * Get all active (incomplete or pending) redblocks that are currently in memory. Completes once the initial cache
     * load has finished.
     *
     * @param filter A filter to apply to the redblocks before returning them
     * @return A list of all active redblocks in memory.
     */
    List<GfRedblock> getRedblocks(Predicate<GfRedblock> filter);

    /**
     * Search the api for redblocks in the project mapped to the given world.
     * @param world The world whose project should be searched
     * @param searchRequest The search request containing the filters to apply
     * @return A page of redblocks that match the search request
     */
    CompletableFuture<Result<GfRedblockSearchResult>> searchRedblocks(World world, GfRedblockSearchRequest searchRequest);

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
     * Edits a redblock. A null or blank message leaves the message unchanged, and null or empty lists make no
     * assignment changes.
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param message The new message of the redblock
     * @param userAdditions The users to assign to the redblock
     * @param userRemovals The users to unassign from the redblock
     * @param roleAdditions The roles to assign to the redblock
     * @param roleRemovals The roles to unassign from the redblock
     * @param editedBy The player who edited the redblock
     * @return The edited redblock
     */
    CompletableFuture<Result<GfRedblock>> editRedblock(String redblockKey, @Nullable String message, @Nullable List<GfUser> userAdditions, @Nullable List<GfUser> userRemovals, @Nullable List<String> roleAdditions, @Nullable List<String> roleRemovals, Player editedBy);

    /**
     * Completes an incomplete redblock, marking it as pending approval
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param completedBy The player who completed the redblock
     * @return The completed redblock
     */
    CompletableFuture<Result<GfRedblock>> completeRedblock(String redblockKey, Player completedBy);

    /**
     * Approves a pending redblock, removing it from the world
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param approvedBy The player who approved the redblock
     * @return The approved redblock
     */
    CompletableFuture<Result<GfRedblock>> approveRedblock(String redblockKey, Player approvedBy);

    /**
     * Denies a pending redblock, marking it as incomplete again
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param deniedBy The player who denied the redblock
     * @return The denied redblock
     */
    CompletableFuture<Result<GfRedblock>> denyRedblock(String redblockKey, Player deniedBy);

    /**
     * Deletes an incomplete or pending redblock, removing it from the world
     * @param redblockKey The PROJECT-KEY of the redblock
     * @param deletedBy The player who deleted the redblock
     * @return The redblock as it was before it was deleted
     */
    CompletableFuture<Result<GfRedblock>> deleteRedblock(String redblockKey, Player deletedBy);

}
