package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class DirectionalInteraction extends InteractionHandler {

    public DirectionalInteraction() {
        super(
                Material.LADDER,
                Material.BIG_DRIPLEAF,
                Material.TRIPWIRE_HOOK,
                Material.REPEATER,
                Material.COMPARATOR
        );
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unnatural placement of directional blocks. (Ladders, tripwire hooks, etc.)");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place a directional block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        if (event.getPlayer().isSneaking() && mat.createBlockData() instanceof Directional block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            block.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placementLocation, mat, block);
        }
    }
}
