package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.type.Observer;
import org.bukkit.event.player.PlayerInteractEvent;

public class ObserverInteraction extends InteractionHandler {

    public ObserverInteraction() {
        super(
                Material.OBSERVER
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            var clickedFace = event.getBlockFace();
            var mat = getHandMat(event);
            var data = (Observer) mat.createBlockData();
            data.setFacing(clickedFace);
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        }
    }
}
