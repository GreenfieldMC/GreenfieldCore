package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.Event;
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
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the modifying and setting of states for blocks that can face multiple directions at one time.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right clicking a block that is multiple facing will toggle the face you clicked on and off.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof MultipleFacing block) {
            var clickedFace = event.getBlockFace();
            if (!block.getAllowedFaces().contains(event.getBlockFace())) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            block.setFace(clickedFace, !block.hasFace(clickedFace));
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), block.getMaterial(), block);
        }
    }
}
