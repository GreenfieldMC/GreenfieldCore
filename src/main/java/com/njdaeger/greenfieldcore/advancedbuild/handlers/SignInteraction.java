package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.services.IWorldEditService;
import com.njdaeger.pdk.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignInteraction extends InteractionHandler {

    public SignInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
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
                Material.MANGROVE_SIGN,
                Material.ACACIA_HANGING_SIGN,
                Material.BAMBOO_HANGING_SIGN,
                Material.BIRCH_HANGING_SIGN,
                Material.CRIMSON_HANGING_SIGN,
                Material.DARK_OAK_HANGING_SIGN,
                Material.JUNGLE_HANGING_SIGN,
                Material.OAK_HANGING_SIGN,
                Material.SPRUCE_HANGING_SIGN,
                Material.WARPED_HANGING_SIGN,
                Material.CHERRY_HANGING_SIGN,
                Material.MANGROVE_HANGING_SIGN
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the unusual placement of signs");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click to place the sign.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var handData = mat.createBlockData();
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            if (handData instanceof HangingSign) {
                var data = (Rotatable) handData;
                var clickedFace = event.getBlockFace();
                data.setRotation(event.getPlayer().getFacing().getOppositeFace());
                if (clickedFace == BlockFace.UP || clickedFace == BlockFace.DOWN) {
                    mat = Material.valueOf(mat.name().replace("_HANGING_SIGN", "_WALL_HANGING_SIGN"));
                }
                placeBlockAt(event.getPlayer(), placementLocation, mat, data);
                return;
            }

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
