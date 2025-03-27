package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.shared.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrushableInteraction extends InteractionHandler {

    private final Map<UUID, Integer> brushesMap = new HashMap<>();

    public BrushableInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() && mainHand.createBlockData() instanceof Brushable;
                },
                Material.SUSPICIOUS_GRAVEL,
                Material.SUSPICIOUS_SAND);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the placement and modification of Suspicious Sand and Gravel.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click to place the block at the desired location. Right click the sand/gravel (when holding the same material) to change how brushed it is.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        var placementLocation = getPlaceableLocation(event);
        if (placementLocation == null) return;

        if (!event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Brushable block) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var lastBrush = brushesMap.getOrDefault(event.getPlayer().getUniqueId(), 0);
            block.setDusted(lastBrush);
            placeBlockAt(event.getPlayer(), placementLocation, handMat, block);
            brushesMap.put(event.getPlayer().getUniqueId(), lastBrush);
        } else if (event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof Brushable block) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var nextBrush = block.getDusted() == block.getMaximumDusted() ? 0 : block.getDusted() + 1;
            block.setDusted(nextBrush);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), handMat, block);
            brushesMap.put(event.getPlayer().getUniqueId(), nextBrush);
        }
    }
}
