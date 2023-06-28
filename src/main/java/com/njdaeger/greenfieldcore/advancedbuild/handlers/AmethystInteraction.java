package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.data.type.AmethystCluster;
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
        return Text.of("Sneak and right click to place an amethyst block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var clicked = event.getClickedBlock();
        if (clicked == null) throw new IllegalStateException("Clicked block was null");

        var face = event.getBlockFace();
        var placeableLocation = getPlaceableLocation(clicked.getLocation(), face);

        if (event.getPlayer().isSneaking()) {

            var data = (AmethystCluster) getHandMat(event).createBlockData();
            data.setFacing(face);

            if (!canPlaceAt(placeableLocation)) {
                event.setCancelled(true);
                return;
            }

            log(false, event.getPlayer(), placeableLocation.getBlock());
            placeableLocation.getBlock().setBlockData(data, false);
            log(true, event.getPlayer(), placeableLocation.getBlock());

        }
    }
}
