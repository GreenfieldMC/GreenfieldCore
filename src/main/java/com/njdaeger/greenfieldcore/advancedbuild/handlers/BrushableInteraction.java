package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrushableInteraction extends InteractionHandler {

    private final Map<UUID, Integer> brushesMap = new HashMap<>();

    public BrushableInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() && mainHand.createBlockData() instanceof Brushable;
                }
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        if (!event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Brushable block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            var lastBrush = brushesMap.getOrDefault(event.getPlayer().getUniqueId(), 0);
            block.setDusted(lastBrush);
            placeBlockAt(event.getPlayer(), placementLocation, handMat, block);
            brushesMap.put(event.getPlayer().getUniqueId(), lastBrush);
        } else if (event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Brushable && event.getClickedBlock().getBlockData() instanceof Brushable block) {
            var nextBrush = block.getDusted() == block.getMaximumDusted() ? 0 : block.getDusted() + 1;
            block.setDusted(nextBrush);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), handMat, block);
            brushesMap.put(event.getPlayer().getUniqueId(), nextBrush);
        }
    }
}
