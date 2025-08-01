package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class WallInteraction extends InteractionHandler {

    public WallInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof Wall;
                }
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement and editing of walls.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click with an empty hand on the side of a wall to cycle between that wall's side heights.");
    }

    @Override
    public TextComponent getMaterialListText() {
        return Component.text("Any block that is a part of the wall group.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof Wall wall) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var clickedFace = event.getBlockFace();
            if (clickedFace == BlockFace.DOWN || clickedFace == BlockFace.UP) {
                wall.setUp(!wall.isUp());
                placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), wall.getMaterial(), wall);
                return;
            }
            var height = wall.getHeight(clickedFace);
            if (height == Wall.Height.NONE) wall.setHeight(clickedFace, Wall.Height.LOW);
            else if (height == Wall.Height.LOW) wall.setHeight(clickedFace, Wall.Height.TALL);
            else wall.setHeight(clickedFace, Wall.Height.NONE);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), wall.getMaterial(), wall);
        }
    }
}
