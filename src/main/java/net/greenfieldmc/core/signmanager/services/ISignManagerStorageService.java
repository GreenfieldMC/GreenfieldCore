package net.greenfieldmc.core.signmanager.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SavedSignGroup;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public interface ISignManagerStorageService extends IModuleService<ISignManagerStorageService> {

    /**
     * Get a saved sign by name.
     * @param name The name of the sign.
     * @return The saved sign, or null if not found.
     */
    @Nullable SavedSign getSign(String name);

    /**
     * Get all saved signs matching a filter.
     * @param filter The filter predicate.
     * @return A list of matching saved signs.
     */
    List<SavedSign> getSigns(Predicate<SavedSign> filter);

    /**
     * Get all saved signs.
     * @return A list of all saved signs.
     */
    default List<SavedSign> getSigns() {
        return getSigns(sign -> true);
    }

    /**
     * Save a sign to the database.
     * @param sign The sign to save.
     */
    void saveSign(SavedSign sign);

    /**
     * Delete a sign by name.
     * @param name The name of the sign to delete.
     */
    void deleteSign(String name);

    /**
     * Get a saved sign group by name.
     * @param name The name of the group.
     * @return The saved sign group, or null if not found.
     */
    @Nullable SavedSignGroup getGroup(String name);

    /**
     * Get all saved sign groups matching a filter.
     * @param filter The filter predicate.
     * @return A list of matching saved sign groups.
     */
    List<SavedSignGroup> getGroups(Predicate<SavedSignGroup> filter);

    /**
     * Get all saved sign groups.
     * @return A list of all saved sign groups.
     */
    default List<SavedSignGroup> getGroups() {
        return getGroups(group -> true);
    }

    /**
     * Save a sign group to the database.
     * @param group The sign group to save.
     */
    void saveGroup(SavedSignGroup group);

    /**
     * Delete a sign group by name.
     * @param name The name of the group to delete.
     */
    void deleteGroup(String name);

    /**
     * Get all distinct group names.
     * @return A list of unique group names.
     */
    List<String> getGroupNames();

    /**
     * Persist all changes to disk.
     */
    void saveDatabase();
}
