package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.player.PlayerInteractEvent;

public class MultipleFacingInteraction extends InteractionHandler {

    public MultipleFacingInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof MultipleFacing;
                }
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof MultipleFacing block) {
            var clickedFace = event.getBlockFace();
            if (!block.getAllowedFaces().contains(event.getBlockFace())) return;
            block.setFace(clickedFace, !block.hasFace(clickedFace));
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), block.getMaterial(), block);
        }
    }
}
