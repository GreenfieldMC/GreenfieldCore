package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.redblock.Redblock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface IRedblockService extends IModuleService<IRedblockService> {

    /**
     * Gets a redblock by its id
     * @param id The id of the redblock
     * @return The redblock
     */
    Redblock getRedblock(int id);

    /**
     * Gets a list of redblocks
     * @param filter The filter to apply to the redblocks before returning them
     * @return The list of redblocks
     */
    List<Redblock> getRedblocks(Predicate<Redblock> filter);

    /**
     * Gets a list of all redblocks
     * @return The list of redblocks
     */
    default List<Redblock> getRedblocks() {
        return getRedblocks(redblock -> true);
    }

    /**
     * Creates a new redblock
     * @param content The content of the redblock
     * @param createdBy The player who created the redblock
     * @param location The location of the redblock
     * @param assignedTo The player the redblock is assigned to, or null if not assigned
     * @param minRank The minimum rank required to complete the redblock, or null if no minimum rank is required
     * @return The created redblock
     */
    Redblock createRedblock(String content, Player createdBy, Location location, @Nullable UUID assignedTo, @Nullable String minRank);

    /**
     * Edits a redblock
     * @param redblock The redblock to edit
     * @param content The new content of the redblock, or null if not changing
     * @param assignedTo The new player the redblock is assigned to. If null, and the redblock is already assigned, it will be unassigned
     * @param minRank The new minimum rank required to complete the redblock. If null, and the redblock has a minimum rank, the minimum rank will be removed
     * @return The edited redblock
     */
    Redblock editRedblock(Redblock redblock, @Nullable String content, @Nullable UUID assignedTo, @Nullable String minRank);

    /**
     * Completes a redblock
     * @param redblock The redblock to complete
     * @param completedBy The player who completed the redblock
     * @return The completed redblock
     */
    Redblock completeRedblock(Redblock redblock, Player completedBy);

    /**
     * Approves a redblock
     * @param redblock The redblock to approve
     * @param approvedBy The player who approved the redblock
     * @return The approved redblock
     */
    Redblock approveRedblock(Redblock redblock, Player approvedBy);

    /**
     * Denies a redblock
     * @param redblock The redblock to deny
     * @return The denied redblock
     */
    Redblock denyRedblock(Redblock redblock);

    /**
     * Deletes a redblock
     * @param redblock The redblock to delete
     * @return The deleted redblock
     */
    Redblock deleteRedblock(Redblock redblock);


}
