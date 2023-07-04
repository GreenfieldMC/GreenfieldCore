package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.type.Observer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class ObserverInteraction extends InteractionHandler {

    public ObserverInteraction() {
        super(
                Material.OBSERVER
        );
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unnatural placement of observers.");
    }

    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place an observer against the blockface you clicked. It flips it 180 degrees from its default position.");
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
