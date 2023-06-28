package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.player.PlayerInteractEvent;

public class BisectedInteraction extends InteractionHandler {

    public BisectedInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() && mainHand.createBlockData() instanceof Bisected;
                }
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        if (event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Bisected block) {
            var clickedFace = event.getBlockFace();
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            var placementOther = clickedFace == BlockFace.DOWN ? placementLocation.clone().subtract(0, 1, 0) : placementLocation.clone().add(0, 1, 0);
            if (!canPlaceAt(placementOther)) return;

            var upperData = (Bisected) block.clone();
            upperData.setHalf(Bisected.Half.TOP);
            var lowerData = (Bisected) block.clone();
            lowerData.setHalf(Bisected.Half.BOTTOM);

            if (clickedFace == BlockFace.DOWN) {
                placeBlockAt(event.getPlayer(), placementOther, handMat, lowerData);
                placeBlockAt(event.getPlayer(), placementLocation, handMat, upperData);
            } else {
                placeBlockAt(event.getPlayer(), placementLocation, handMat, lowerData);
                placeBlockAt(event.getPlayer(), placementOther, handMat, upperData);
            }
        }
    }
}
