package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
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
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        if (event.getPlayer().isSneaking() && mat.createBlockData() instanceof Directional block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            block.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placementLocation, mat, block);
        }
    }
}
