package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.player.PlayerInteractEvent;

public class WallInteraction extends InteractionHandler {

    public WallInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof Wall;
                }
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof Wall wall) {
            var clickedFace = event.getBlockFace();
            var height = wall.getHeight(clickedFace);
            if (height == Wall.Height.NONE) wall.setHeight(clickedFace, Wall.Height.LOW);
            else if (height == Wall.Height.LOW) wall.setHeight(clickedFace, Wall.Height.TALL);
            else wall.setHeight(clickedFace, Wall.Height.NONE);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), wall.getMaterial(), wall);
        }
    }
}
