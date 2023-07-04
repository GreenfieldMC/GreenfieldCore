package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class MangroveRootsInteraction extends InteractionHandler {

    public MangroveRootsInteraction() {
        super(
                Material.MANGROVE_ROOTS
        );
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Toggle the waterlogged state of mangrove roots.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Right click a mangrove root (when the mangrove root is in hand) to toggle its waterlogged state.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking() && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.MANGROVE_ROOTS && event.getClickedBlock().getBlockData() instanceof Waterlogged block) {
            block.setWaterlogged(!block.isWaterlogged());
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), block.getMaterial(), block);
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }
}
