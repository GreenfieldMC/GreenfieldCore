package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class DoorInteraction extends InteractionHandler {

    public DoorInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
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
                Material.COPPER_DOOR,
                Material.EXPOSED_COPPER_DOOR,
                Material.OXIDIZED_COPPER_DOOR,
                Material.PALE_OAK_DOOR,
                Material.WAXED_COPPER_DOOR,
                Material.WAXED_EXPOSED_COPPER_DOOR,
                Material.WAXED_OXIDIZED_COPPER_DOOR,
                Material.WAXED_WEATHERED_COPPER_DOOR,
                Material.WEATHERED_COPPER_DOOR);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of doors.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a door against/on the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var clickedFace = event.getBlockFace();
            var playerDirection = event.getPlayer().getFacing();
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            var direction = getRequiredDirection(event.getClickedPosition(), playerDirection, clickedFace);
            var hinge = getRequiredHinge(event.getClickedPosition(), playerDirection, direction);

            var otherPlacementHalf = clickedFace == BlockFace.DOWN ? placementLocation.clone().subtract(0, 1, 0) : placementLocation.clone().add(0, 1, 0);
            if (!canPlaceAt(otherPlacementHalf)) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            var dataBottom = (Door) getHandMat(event).createBlockData();
            dataBottom.setHinge(hinge);
            dataBottom.setFacing(direction);
            dataBottom.setHalf(clickedFace == BlockFace.DOWN ? Door.Half.TOP : Door.Half.BOTTOM);
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event), dataBottom);


            var dataTop = (Door) getHandMat(event).createBlockData();
            dataTop.setHinge(hinge);
            dataTop.setFacing(direction);
            dataTop.setHalf(clickedFace == BlockFace.DOWN ? Door.Half.BOTTOM : Door.Half.TOP);
            placeBlockAt(event.getPlayer(), otherPlacementHalf, getHandMat(event), dataTop);
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
