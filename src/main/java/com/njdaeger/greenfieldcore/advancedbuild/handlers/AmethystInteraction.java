package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.type.AmethystCluster;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class AmethystInteraction extends InteractionHandler {

    public AmethystInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.SMALL_AMETHYST_BUD,
                Material.MEDIUM_AMETHYST_BUD,
                Material.LARGE_AMETHYST_BUD,
                Material.AMETHYST_CLUSTER);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of amethyst clusters.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place an amethyst block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var placementLocation = getPlaceableLocation(event);
        if (placementLocation == null) return;
        if (event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            var data = (AmethystCluster) mat.createBlockData();
            var waterlog = placementLocation.getBlock().getType() == Material.WATER;
            data.setFacing(event.getBlockFace());
            data.setWaterlogged(waterlog);

            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        }
    }
}
