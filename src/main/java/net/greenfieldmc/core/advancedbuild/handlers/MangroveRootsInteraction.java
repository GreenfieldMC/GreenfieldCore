package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class MangroveRootsInteraction extends InteractionHandler {

    public MangroveRootsInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.MANGROVE_ROOTS
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Toggle the waterlogged state of mangrove roots.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Right click a mangrove root (when the mangrove root is in hand) to toggle its waterlogged state.");
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
