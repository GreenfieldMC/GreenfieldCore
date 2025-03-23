package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.services.IWorldEditService;
import org.bukkit.Material;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.event.player.PlayerInteractEvent;

public class LightningRodInteraction extends InteractionHandler {

    public LightningRodInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, Material.LIGHTNING_ROD);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var handData = mat.createBlockData();
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            if (handData instanceof LightningRod rod) {
                rod.setFacing(event.getBlockFace().getOppositeFace());
                placeBlockAt(event.getPlayer(), placementLocation, mat, rod);
            }
        }
    }
}
