package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.type.AmethystCluster;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class AmethystInteraction extends InteractionHandler {

    public AmethystInteraction() {
        super(
                Material.SMALL_AMETHYST_BUD,
                Material.MEDIUM_AMETHYST_BUD,
                Material.LARGE_AMETHYST_BUD,
                Material.AMETHYST_CLUSTER);
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unnatural placement of amethyst clusters.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place an amethyst block against the blockface you clicked.");
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
