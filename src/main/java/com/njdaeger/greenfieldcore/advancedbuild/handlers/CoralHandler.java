package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

public class CoralHandler extends BlockHandler {

    public CoralHandler() {
        super(
                Material.DEAD_BRAIN_CORAL_FAN,
                Material.DEAD_BUBBLE_CORAL_FAN,
                Material.DEAD_FIRE_CORAL_FAN,
                Material.DEAD_HORN_CORAL_FAN,
                Material.DEAD_TUBE_CORAL_FAN,
                Material.DEAD_BRAIN_CORAL_WALL_FAN,
                Material.DEAD_BUBBLE_CORAL_WALL_FAN,
                Material.DEAD_FIRE_CORAL_WALL_FAN,
                Material.DEAD_HORN_CORAL_WALL_FAN,
                Material.DEAD_TUBE_CORAL_WALL_FAN);
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        BlockData data = placeMaterial.createBlockData();
        if (clickedFace != BlockFace.DOWN && clickedFace != BlockFace.UP) {
            Material wallMat = Material.getMaterial(placeMaterial.name().split("_FAN")[0] + "_WALL_FAN");
            if (wallMat != null) {
                placeMaterial = wallMat;
                data = placeMaterial.createBlockData();
                ((Directional) data).setFacing(clickedFace);
            }
        }
        if (data instanceof Waterlogged w) {
            w.setWaterlogged(false);
        }
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(data, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }
}
