package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
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
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unusual placement of chorus plants and flowers.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place a chorus plant or flower against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMaterial = getHandMat(event);
        var placeable = getPlaceableLocation(event);
        if (placeable == null || !event.getPlayer().isSneaking()) return;
        if (handMaterial == Material.CHORUS_PLANT) {
            var plant = (MultipleFacing) handMaterial.createBlockData();
            if (!plant.getAllowedFaces().contains(event.getBlockFace())) return;
            plant.setFace(event.getBlockFace().getOppositeFace(), true);

            event.setCancelled(true);
            event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
            event.setUseItemInHand(PlayerInteractEvent.Result.DENY);

            placeBlockAt(event.getPlayer(), placeable, handMaterial, plant);

        } else {
            event.setCancelled(true);
            event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
            event.setUseItemInHand(PlayerInteractEvent.Result.DENY);

            placeBlockAt(event.getPlayer(), placeable, handMaterial);
        }

//        var clickedBlock = event.getClickedBlock();
//        if (clickedBlock.getType() == Material.CHORUS_PLANT) {
//            var plant = (MultipleFacing) clickedBlock.getBlockData();
//            var clickedFace = event.getBlockFace();
//            if (!plant.getAllowedFaces().contains(event.getBlockFace())) return;
//            plant.setFace(clickedFace, true);
//
//            log(false, event.getPlayer(), clickedBlock);
//            clickedBlock.setBlockData(plant, false);
//            log(true, event.getPlayer(), clickedBlock);
//        }
    }
}
