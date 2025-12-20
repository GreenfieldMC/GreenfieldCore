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
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class PressurePlateInteraction extends InteractionHandler {

    private final Map<UUID, Boolean> sessions = new HashMap<>();

    public PressurePlateInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
            Block block = event.getClickedBlock();
            if (block != null && isPressurePlate(block.getType())) return true;
            Material mat = event.getMaterial();
            return mat != null && isPressurePlate(mat);
        }, Material.STONE_PRESSURE_PLATE);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Shift right-click with empty hand to toggle pressure plate powered state. Shift-place to use saved state.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right-click to toggle powered. Shift-place to use saved powered state.").color(NamedTextColor.GRAY);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        UUID uuid = event.getPlayer().getUniqueId();
        boolean isEmptyHand = event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR;

        if (event.getPlayer().isSneaking() && isEmptyHand) {
            if (!(block.getBlockData() instanceof Powerable powerable)) return;
            boolean newPowered = !powerable.isPowered();
            powerable.setPowered(newPowered);
            sessions.put(uuid, newPowered);
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), block.getLocation(), block.getType(), powerable);
        } else if (event.getPlayer().isSneaking()) {
            var placeLoc = getPlaceableLocation(event);
            if (placeLoc != null) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                Powerable powerable = (Powerable) event.getMaterial().createBlockData();
                powerable.setPowered(sessions.getOrDefault(uuid, false));
                placeBlockAt(event.getPlayer(), placeLoc, event.getMaterial(), powerable);
            }
        }
    }

    private static boolean isPressurePlate(Material mat) {
        String name = mat.name();
        return name.endsWith("_PRESSURE_PLATE");
    }
}