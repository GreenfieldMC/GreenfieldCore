package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.Event;
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
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unnatural placement and editing of walls.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift right click with an empty hand on the side of a wall to cycle between that wall's side heights.");
    }

    @Override
    public Text.Section getMaterialListText() {
        return Text.of("Any block that is a part of the wall group.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof Wall wall) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var clickedFace = event.getBlockFace();
            var height = wall.getHeight(clickedFace);
            if (height == Wall.Height.NONE) wall.setHeight(clickedFace, Wall.Height.LOW);
            else if (height == Wall.Height.LOW) wall.setHeight(clickedFace, Wall.Height.TALL);
            else wall.setHeight(clickedFace, Wall.Height.NONE);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), wall.getMaterial(), wall);
        }
    }
}
