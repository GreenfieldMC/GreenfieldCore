package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

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
                Material.PALE_MOSS_CARPET,
                Material.ACACIA_PRESSURE_PLATE,
                Material.BIRCH_PRESSURE_PLATE,
                Material.CRIMSON_PRESSURE_PLATE,
                Material.DARK_OAK_PRESSURE_PLATE,
                Material.JUNGLE_PRESSURE_PLATE,
                Material.OAK_PRESSURE_PLATE,
                Material.SPRUCE_PRESSURE_PLATE,
                Material.STONE_PRESSURE_PLATE,
                Material.WARPED_PRESSURE_PLATE,
                Material.MANGROVE_PRESSURE_PLATE,
                Material.CHERRY_PRESSURE_PLATE,
                Material.BAMBOO_PRESSURE_PLATE,
                Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                Material.PALE_OAK_PRESSURE_PLATE,
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE
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
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event));
        }
    }
}
