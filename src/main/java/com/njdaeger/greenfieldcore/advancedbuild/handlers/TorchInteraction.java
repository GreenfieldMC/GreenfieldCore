package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.player.PlayerInteractEvent;

public class TorchInteraction extends InteractionHandler {

    public TorchInteraction() {
        super(
                Material.SOUL_TORCH,
                Material.REDSTONE_TORCH,
                Material.TORCH
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var placementLocation = getPlaceableLocation(event);
        if (placementLocation == null || !event.getPlayer().isSneaking()) return;

        if (event.getBlockFace() != BlockFace.UP) {
            mat = switch (mat) {
                case SOUL_TORCH -> Material.SOUL_WALL_TORCH;
                case REDSTONE_TORCH -> Material.REDSTONE_WALL_TORCH;
                case TORCH -> Material.WALL_TORCH;
                default -> throw new RuntimeException("Unknown torch type: " + mat);
            };
            var data = (Directional)mat.createBlockData();
            data.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        } else placeBlockAt(event.getPlayer(), placementLocation, mat);
    }
}
