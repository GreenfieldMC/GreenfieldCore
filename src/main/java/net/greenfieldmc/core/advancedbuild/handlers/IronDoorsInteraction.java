// src/main/java/net/greenfieldmc/core/advancedbuild/handlers/IronDoorsInteraction.java
package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IronDoorsInteraction extends InteractionHandler {

    private final Map<UUID, Boolean> sessions = new HashMap<>();

    public IronDoorsInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        // update constructor predicate to also allow when holding the item being placed
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
            Block clicked = event.getClickedBlock();
            return clicked != null && (clicked.getType() == Material.IRON_DOOR || clicked.getType() == Material.IRON_TRAPDOOR);
        }, Material.IRON_DOOR, Material.IRON_TRAPDOOR);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Shift right-click with empty hand to toggle iron door/trapdoor open state. Shift-place to use saved state.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right-click to toggle open. Shift-place to use saved open state.").color(NamedTextColor.GRAY);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        // in onRightClickBlock, derive type from clicked block or from the held item when placing
        boolean isEmptyHand = event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR;
        UUID uuid = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();

        if (isEmptyHand && player.isSneaking()) {

            if (block.getBlockData() instanceof Door door) {
                boolean newOpen = !door.isOpen();
                door.setOpen(newOpen);
                block.setBlockData(door, false);

                // Update the other half
                Block otherHalf = (door.getHalf() == Door.Half.TOP)
                        ? block.getRelative(BlockFace.DOWN)
                        : block.getRelative(BlockFace.UP);
                if (otherHalf.getType() == Material.IRON_DOOR && otherHalf.getBlockData() instanceof Door otherDoor) {
                    otherDoor.setOpen(newOpen);
                    otherHalf.setBlockData(otherDoor, false);
                }

                sessions.put(uuid, newOpen);
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                placeBlockAt(player, block.getLocation(), Material.IRON_DOOR, door);
            } else if (block.getBlockData() instanceof TrapDoor trapDoor) {
                // Iron trapdoor logic
                boolean newOpen = !trapDoor.isOpen();
                trapDoor.setOpen(newOpen);
                block.setBlockData(trapDoor, false);

                sessions.put(uuid, newOpen);
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                placeBlockAt(player, block.getLocation(), Material.IRON_TRAPDOOR, trapDoor);
            }
        }

        // Shift-place: use saved open state
        else if (!isEmptyHand && player.isSneaking() && (event.getItem().getType() == Material.IRON_DOOR || event.getItem().getType() == Material.IRON_TRAPDOOR)) {
            var placeLoc = getPlaceableLocation(event);

            if (placeLoc != null) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);

                if (block.getBlockData()instanceof Door) {
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

            } else if (block.getBlockData()instanceof TrapDoor) {
                org.bukkit.block.data.type.TrapDoor trapDoor = (org.bukkit.block.data.type.TrapDoor) Material.IRON_TRAPDOOR.createBlockData();
                trapDoor.setOpen(sessions.getOrDefault(uuid, false));
                placeBlockAt(player, placeLoc, Material.IRON_TRAPDOOR, trapDoor);
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