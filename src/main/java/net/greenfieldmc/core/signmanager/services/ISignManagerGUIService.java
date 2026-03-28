package net.greenfieldmc.core.signmanager.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.signmanager.SignFlag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface ISignManagerGUIService extends IModuleService<ISignManagerGUIService> {

    /**
     * Open the sign manager GUI for a player showing all signs.
     * @param player The player to open the GUI for.
     */
    void openGUI(Player player);

    /**
     * Open the sign manager GUI filtered by a search query.
     * @param player The player to open the GUI for.
     * @param query The search query.
     */
    void openGUIWithSearch(Player player, String query);

    /**
     * Open the sign manager GUI filtered by a flag.
     * @param player The player to open the GUI for.
     * @param flag The flag to filter by.
     */
    void openGUIWithFlag(Player player, SignFlag flag);

    /**
     * Check if a player has the GUI open.
     * @param player The player.
     * @return true if the player has the GUI open.
     */
    boolean hasGUIOpen(Player player);
}

