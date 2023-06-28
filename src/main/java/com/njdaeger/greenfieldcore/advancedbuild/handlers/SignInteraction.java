package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
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
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
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
