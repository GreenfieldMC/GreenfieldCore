package net.greenfieldmc.core.shared.services;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public interface ICoreProtectService {

    /**
     * @return true if CoreProtect's API is currently enabled. When false, the log methods do nothing.
     */
    boolean isEnabled();

    void logPlacement(String player, Location location, Material type, BlockData blockdata);

    void logRemoval(String player, Location location, Material type, BlockData blockdata);

    void logContainerTransaction(String player, Location location);

}
