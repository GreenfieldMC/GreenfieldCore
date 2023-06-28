package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class DoorInteraction extends InteractionHandler {

    public DoorInteraction() {
        super(
                Material.ACACIA_DOOR,
                Material.BIRCH_DOOR,
                Material.DARK_OAK_DOOR,
                Material.JUNGLE_DOOR,
                Material.OAK_DOOR,
                Material.SPRUCE_DOOR,
                Material.MANGROVE_DOOR,
                Material.CHERRY_DOOR,
                Material.BAMBOO_DOOR,
                Material.CRIMSON_DOOR,
                Material.WARPED_DOOR,
                Material.IRON_DOOR);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var clickedFace = event.getBlockFace();
            var playerDirection = event.getPlayer().getFacing();
            var placementLocation = getPlaceableLocation(event.getClickedBlock().getLocation(), clickedFace);

            var direction = getRequiredDirection(event.getClickedPosition(), playerDirection, clickedFace);
            var hinge = getRequiredHinge(event.getClickedPosition(), playerDirection, direction);

            if (canPlaceAt(placementLocation)) {
                var otherPlacementHalf = clickedFace == BlockFace.DOWN ? placementLocation.clone().subtract(0, 1, 0) : placementLocation.clone().add(0, 1, 0);
                if (!canPlaceAt(otherPlacementHalf)) return;
                var dataBottom = (Door) getHandMat(event).createBlockData();
                dataBottom.setHinge(hinge);
                dataBottom.setFacing(direction);
                dataBottom.setHalf(clickedFace == BlockFace.DOWN ? Door.Half.TOP : Door.Half.BOTTOM);
                log(false, event.getPlayer(), placementLocation.getBlock());
                placementLocation.getBlock().setType(getHandMat(event), false);
                placementLocation.getBlock().setBlockData(dataBottom, false);
                log(true, event.getPlayer(), placementLocation.getBlock());


                var dataTop = (Door) getHandMat(event).createBlockData();
                dataTop.setHinge(hinge);
                dataTop.setFacing(direction);
                dataTop.setHalf(clickedFace == BlockFace.DOWN ? Door.Half.BOTTOM : Door.Half.TOP);
                log(false, event.getPlayer(), otherPlacementHalf.getBlock());
                otherPlacementHalf.getBlock().setType(getHandMat(event), false);
                otherPlacementHalf.getBlock().setBlockData(dataTop, false);
                log(true, event.getPlayer(), otherPlacementHalf.getBlock());
            }
        }
    }

    private static BlockFace getRequiredDirection(Vector clickedLocation, BlockFace playerDirection, BlockFace clickedFace) {
        if (clickedFace != BlockFace.UP && clickedFace != BlockFace.DOWN) {
            return clickedFace;
        } else {
            var facingFactor = (playerDirection == BlockFace.NORTH || playerDirection == BlockFace.SOUTH) ?
                    (playerDirection == BlockFace.SOUTH ? 1 - clickedLocation.getZ() : clickedLocation.getZ()) :
                    (playerDirection == BlockFace.EAST ? 1 - clickedLocation.getX() : clickedLocation.getX());
            return facingFactor >= 0.5d ? playerDirection : playerDirection.getOppositeFace();
        }
    }

    private static Door.Hinge getRequiredHinge(Vector clickedLocation, BlockFace playerDirection, BlockFace requiredDirection) {
        var leftHingeFactor = playerDirection == BlockFace.NORTH || playerDirection == BlockFace.SOUTH ?
                (playerDirection == BlockFace.SOUTH ? 1 - clickedLocation.getX() : clickedLocation.getX()) :
                (playerDirection == BlockFace.WEST ? 1 - clickedLocation.getZ() : clickedLocation.getZ());
        if (playerDirection == requiredDirection) return leftHingeFactor >= 0.5d ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
        else return leftHingeFactor >= 0.5d ? Door.Hinge.LEFT : Door.Hinge.RIGHT;
    }
}
