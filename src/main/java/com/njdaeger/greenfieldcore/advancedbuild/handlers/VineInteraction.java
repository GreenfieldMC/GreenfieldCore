package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.player.PlayerInteractEvent;

public class VineInteraction extends InteractionHandler {

    public VineInteraction() {
        super(
                Material.VINE,
                Material.GLOW_LICHEN
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && getHandMat(event).createBlockData() instanceof MultipleFacing facing) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            facing.setFace(event.getBlockFace().getOppositeFace(), true);
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event), facing);
        }
    }
}
