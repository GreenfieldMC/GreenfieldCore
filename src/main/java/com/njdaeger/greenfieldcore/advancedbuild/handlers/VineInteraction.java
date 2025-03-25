package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class VineInteraction extends InteractionHandler {

    public VineInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.VINE,
                Material.GLOW_LICHEN
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of vines.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a vine against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && getHandMat(event).createBlockData() instanceof MultipleFacing facing) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            facing.setFace(event.getBlockFace().getOppositeFace(), true);
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event), facing);
        }
    }
}
