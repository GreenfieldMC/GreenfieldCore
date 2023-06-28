package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChorusInteraction extends InteractionHandler {

    public ChorusInteraction() {
        super(
                Material.CHORUS_PLANT,
                Material.CHORUS_FLOWER
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMaterial = getHandMat(event);
        if (handMaterial == Material.CHORUS_PLANT) {
            var placeable = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
            if (canPlaceAt(placeable)) {

                var plant = (MultipleFacing) handMaterial.createBlockData();
                if (!plant.getAllowedFaces().contains(event.getBlockFace())) return;
                plant.setFace(event.getBlockFace().getOppositeFace(), true);

                event.setCancelled(true);
                event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
                event.setUseItemInHand(PlayerInteractEvent.Result.DENY);

                log(false, event.getPlayer(), placeable.getBlock());
                placeable.getBlock().setType(handMaterial, false);
                placeable.getBlock().setBlockData(plant, false);
                playSoundFor(true, event.getPlayer(), handMaterial);
                log(true, event.getPlayer(), placeable.getBlock());
            }
        } else {
            event.setCancelled(true);
            event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
            event.setUseItemInHand(PlayerInteractEvent.Result.DENY);
            var placeable = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
            if (canPlaceAt(placeable)) {
                log(false, event.getPlayer(), placeable.getBlock());
                placeable.getBlock().setType(handMaterial, false);
                playSoundFor(true, event.getPlayer(), handMaterial);
                log(true, event.getPlayer(), placeable.getBlock());
            }
        }

        var clickedBlock = event.getClickedBlock();
        if (clickedBlock.getType() == Material.CHORUS_PLANT) {
            var plant = (MultipleFacing) clickedBlock.getBlockData();
            var clickedFace = event.getBlockFace();
            if (!plant.getAllowedFaces().contains(event.getBlockFace())) return;
            plant.setFace(clickedFace, true);

            log(false, event.getPlayer(), clickedBlock);
            clickedBlock.setBlockData(plant, false);
            log(true, event.getPlayer(), clickedBlock);
        }
    }
}
