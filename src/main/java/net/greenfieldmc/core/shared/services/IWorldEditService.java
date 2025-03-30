package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IModuleService;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public interface IWorldEditService extends IModuleService<IWorldEditService> {

    void setBlock(Location location, BlockData data);

}
