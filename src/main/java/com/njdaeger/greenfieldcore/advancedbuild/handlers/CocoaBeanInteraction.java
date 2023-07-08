package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CocoaBeanInteraction extends InteractionHandler {

    private final Map<UUID, Integer> lastAge = new HashMap<>();

    public CocoaBeanInteraction() {
        super(Material.COCOA_BEANS);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var placeableLocation = getPlaceableLocation(event);
            if (placeableLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var data = (Cocoa) Material.COCOA.createBlockData();
            lastAge.putIfAbsent(event.getPlayer().getUniqueId(), 0);
            data.setAge(lastAge.get(event.getPlayer().getUniqueId()));
            data.setFacing(event.getBlockFace().getOppositeFace());
            placeBlockAt(event.getPlayer(), placeableLocation, Material.COCOA, data);
        } else if (event.getClickedBlock().getType() == Material.COCOA && event.getClickedBlock().getBlockData() instanceof Cocoa c) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            c.setAge(c.getMaximumAge() == c.getAge() ? 0 : c.getAge() + 1);
            lastAge.put(event.getPlayer().getUniqueId(), c.getAge());
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), Material.COCOA, c);
        }
    }
}
