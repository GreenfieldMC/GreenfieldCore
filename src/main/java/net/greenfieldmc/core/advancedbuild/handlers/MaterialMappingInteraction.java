package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class MaterialMappingInteraction extends InteractionHandler {

    private static final Map<Material, Material> materialMap = new HashMap<>() {{
        put(Material.SWEET_BERRIES, Material.SWEET_BERRY_BUSH);
    }};

    public MaterialMappingInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, materialMap.keySet().toArray(new Material[0]));
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Place one material as another.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click a block to place the mapped material instead.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var handMat = getHandMat(event);
            if (!materialMap.containsKey(handMat)) return;
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), placementLocation, materialMap.get(handMat));
        }
    }
}

