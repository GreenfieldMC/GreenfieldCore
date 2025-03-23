package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public interface IWorldEditService extends IModuleService<IWorldEditService> {

    void setBlock(Location location, BlockData data);

}
