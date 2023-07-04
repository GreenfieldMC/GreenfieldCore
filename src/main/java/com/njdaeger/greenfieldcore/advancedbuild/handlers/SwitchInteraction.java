package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.player.PlayerInteractEvent;

public class SwitchInteraction extends InteractionHandler {

    public SwitchInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() && mainHand.createBlockData() instanceof Switch;
                }
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        if (event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Switch block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            if (event.getBlockFace() == BlockFace.DOWN) block.setAttachedFace(FaceAttachable.AttachedFace.CEILING);
            else if (event.getBlockFace() == BlockFace.UP) block.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
            else block.setAttachedFace(FaceAttachable.AttachedFace.WALL);

            if (!block.getFaces().contains(event.getBlockFace())) block.setFacing(event.getPlayer().getFacing().getOppositeFace());
            else block.setFacing(event.getBlockFace());

            placeBlockAt(event.getPlayer(), placementLocation, handMat, block);
        }
    }
}
