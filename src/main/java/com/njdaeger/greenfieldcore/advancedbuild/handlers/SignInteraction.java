package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignInteraction extends InteractionHandler {

    public SignInteraction() {
        super(
                Material.ACACIA_SIGN,
                Material.BAMBOO_SIGN,
                Material.BIRCH_SIGN,
                Material.CRIMSON_SIGN,
                Material.DARK_OAK_SIGN,
                Material.JUNGLE_SIGN,
                Material.OAK_SIGN,
                Material.SPRUCE_SIGN,
                Material.WARPED_SIGN,
                Material.CHERRY_SIGN,
                Material.MANGROVE_SIGN
        );
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allow the unusual placement of signs");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift right click to place the sign.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            var clickedFace = event.getBlockFace();
            if (clickedFace == BlockFace.UP || clickedFace == BlockFace.DOWN) return;
            var name = mat.name().replace("_SIGN", "_WALL_SIGN");
            mat = Material.valueOf(name);
            var data = (WallSign) mat.createBlockData();
            data.setFacing(clickedFace);
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        }
    }
}
