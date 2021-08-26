package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public class TorchHandler extends BlockHandler {

    public TorchHandler() {
        super(
                Material.TORCH,
                Material.SOUL_TORCH,
                Material.REDSTONE_TORCH
        );
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        BlockData data = placeMaterial.createBlockData();
        if (clickedFace != BlockFace.UP) {
            Material wallMat = Material.getMaterial(placeMaterial.name().replace("TORCH", "WALL_TORCH"));
            if (wallMat != null) {
                placeMaterial = wallMat;
                data = placeMaterial.createBlockData();
                ((Directional) data).setFacing(player.getFacing().getOppositeFace());
            }
        }
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(data, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }
}
