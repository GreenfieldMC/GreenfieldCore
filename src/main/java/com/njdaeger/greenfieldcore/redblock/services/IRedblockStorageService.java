package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.redblock.Redblock;

import java.util.List;
import java.util.function.Predicate;

public interface IRedblockStorageService extends IModuleService<IRedblockStorageService> {

    /**
     * Gets the next id for a redblock
     * @return The next id
     */
    int getNextId();

    /**
     * Gets the next id for a redblock and increments it
     * @return The next id before incrementing
     */
    int getNextAndIncrementId();

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
     * Saves a redblock to memory, not the database... yet
     * @param redblock The redblock to save
     */
    void saveRedblock(Redblock redblock);

    /**
     * Saves the redblocks to the database
     */
    void saveDatabase();
}
