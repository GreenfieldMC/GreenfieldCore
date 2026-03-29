package net.greenfieldmc.core.signmanager.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SavedSignGroup;
import net.greenfieldmc.core.signmanager.SignManagerEntry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ISignManagerService extends IModuleService<ISignManagerService> {

    /**
     * Save a sign from the player's main hand.
     * @param player The player holding the sign.
     * @param name The name to save the sign under.
     * @return The saved sign, or null if the player is not holding a sign.
     */
    @Nullable SavedSign saveSignFromHand(Player player, String name);

    /**
     * Save all sign items from the player's hotbar as a group.
     * The main hand item is used as the display sign.
     * @param player The player whose hotbar to scan.
     * @param groupName The group name.
     * @return The saved group, or null if no signs found in hotbar or main hand is not a sign.
     */
    @Nullable SavedSignGroup saveGroupFromHotbar(Player player, String groupName);

    /**
     * Delete a saved sign by name.
     * @param name The name of the sign.
     * @return true if the sign was found and deleted.
     */
    boolean deleteSign(String name);

    /**
     * Delete a saved sign group by name.
     * @param groupName The group name.
     * @return true if the group was found and deleted.
     */
    boolean deleteGroup(String groupName);

    /**
     * Get a saved sign by name.
     * @param name The name of the sign.
     * @return The saved sign, or null if not found.
     */
    @Nullable SavedSign getSign(String name);

    /**
     * Check if a name is already taken by a sign or group.
     * @param name The name to check.
     * @return true if the name is in use.
     */
    boolean nameExists(String name);

    /**
     * Get all GUI entries (individual signs + groups as single entries), sorted alphabetically.
     * @return A list of all entries.
     */
    List<SignManagerEntry> getAllEntries();

    /**
     * Fuzzy search entries by name and content.
     * @param query The search query.
     * @return A list of matching entries.
     */
    List<SignManagerEntry> searchEntries(String query);

    /**
     * Give a player all sign items for an entry by name.
     * @param player The player to give signs to.
     * @param entryName The name of the sign or group entry.
     * @return The entry that was given, or null if not found.
     */
    @Nullable SignManagerEntry giveEntry(Player player, String entryName);

    /**
     * Get all distinct group names.
     * @return A list of group names.
     */
    List<String> getGroupNames();

    /**
     * Get all individual saved signs (not groups).
     * @return A list of all individual saved signs.
     */
    List<SavedSign> getAllSigns();

    /**
     * Get all sign names (individual signs only, not groups).
     * @return A list of sign names.
     */
    List<String> getSignNames();

    /**
     * Get all entry names (signs + groups).
     * @return A list of all entry names.
     */
    List<String> getAllEntryNames();
}
