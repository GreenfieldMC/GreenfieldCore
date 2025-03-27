package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.shared.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.type.Observer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class ObserverInteraction extends InteractionHandler {

    public ObserverInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.OBSERVER
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of observers.");
    }

    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place an observer against the blockface you clicked. It flips it 180 degrees from its default position.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var clickedFace = event.getBlockFace();
            var mat = getHandMat(event);
            var data = (Observer) mat.createBlockData();
            data.setFacing(clickedFace);
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        }
    }
}
