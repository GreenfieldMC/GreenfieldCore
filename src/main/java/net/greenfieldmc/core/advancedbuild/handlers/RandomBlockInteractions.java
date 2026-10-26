package net.greenfieldmc.core.advancedbuild.handlers;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlockItemDataProperties;
import io.papermc.paper.registry.keys.DataComponentTypeKeys;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class RandomBlockInteractions extends InteractionHandler {

    public RandomBlockInteractions(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.BLACK_CARPET,
                Material.BLUE_CARPET,
                Material.BROWN_CARPET,
                Material.CYAN_CARPET,
                Material.GRAY_CARPET,
                Material.GREEN_CARPET,
                Material.LIGHT_BLUE_CARPET,
                Material.LIGHT_GRAY_CARPET,
                Material.LIME_CARPET,
                Material.MAGENTA_CARPET,
                Material.ORANGE_CARPET,
                Material.PINK_CARPET,
                Material.PURPLE_CARPET,
                Material.RED_CARPET,
                Material.WHITE_CARPET,
                Material.YELLOW_CARPET,
                Material.MOSS_CARPET,
                Material.LIGHT
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the unusual placement of various blocks.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click to place the block.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var assignedBlockdata = event.getItem().getData(DataComponentTypes.BLOCK_DATA);
            if (assignedBlockdata != null) {
                var handMaterial = getHandMat(event);
                placeBlockAt(event.getPlayer(), placementLocation, handMaterial, assignedBlockdata.applyTo(handMaterial.createBlockData()));
                return;
            }
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event));
        }
    }
}
