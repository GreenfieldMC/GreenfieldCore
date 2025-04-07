package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IModuleService;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public interface IWorldEditService extends IModuleService<IWorldEditService> {

    /**
     * Set a block at the given location to the given block data
     *
     * @param location the location to set the block at
     * @param data     the block data to set
     */
    void setBlock(Location location, BlockData data);

}
