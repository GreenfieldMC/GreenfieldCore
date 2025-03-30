package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IModuleService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public interface ICoreProtectService extends IModuleService<ICoreProtectService> {

    void logPlacement(String player, Location location, Material type, BlockData blockdata);

    void logRemoval(String player, Location location, Material type, BlockData blockdata);

}
