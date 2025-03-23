package com.njdaeger.greenfieldcore.services;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class WorldEditServiceActualImpl {

    private final WorldEdit worldEdit;

    public WorldEditServiceActualImpl(WorldEditPlugin worldEditPlugin) {
        this.worldEdit = worldEditPlugin.getWorldEdit();
    }

    public void setBlock(Location location, BlockData data) {
        var world = worldEdit.getPlatformManager().getWorldForEditing(BukkitAdapter.adapt(location.getWorld()));
        var block = BukkitAdapter.adapt(data).toBaseBlock();
        try {
            world.setBlock(BukkitAdapter.asBlockVector(location), block, SideEffectSet.none());
        } catch (WorldEditException e) {
            throw new RuntimeException("Failed to set block at " + location, e);
        }
    }
}
