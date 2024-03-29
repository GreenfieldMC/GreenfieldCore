package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class VineInteraction extends InteractionHandler {

    public VineInteraction() {
        super(
                Material.VINE,
                Material.GLOW_LICHEN
        );
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unnatural placement of vines.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place a vine against the blockface you clicked.");
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
