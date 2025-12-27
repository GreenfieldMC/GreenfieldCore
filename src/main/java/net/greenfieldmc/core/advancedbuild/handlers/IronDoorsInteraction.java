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

import static net.greenfieldmc.core.advancedbuild.handlers.DoorInteraction.getRequiredDirection;
import static net.greenfieldmc.core.advancedbuild.handlers.DoorInteraction.getRequiredHinge;

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
    }
}