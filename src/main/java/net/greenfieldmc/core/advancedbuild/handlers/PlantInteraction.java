package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlantInteraction extends InteractionHandler {

    public PlantInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.OAK_SAPLING,
                Material.SPRUCE_SAPLING,
                Material.BIRCH_SAPLING,
                Material.JUNGLE_SAPLING,
                Material.ACACIA_SAPLING,
                Material.DARK_OAK_SAPLING,
                Material.CHERRY_SAPLING,
                Material.SHORT_GRASS,
                Material.FERN,
                Material.DEAD_BUSH,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.CORNFLOWER,
                Material.LILY_OF_THE_VALLEY,
                Material.WITHER_ROSE,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.CRIMSON_FUNGUS,
                Material.WARPED_FUNGUS,
                Material.CRIMSON_ROOTS,
                Material.WARPED_ROOTS,
                Material.NETHER_SPROUTS,
                Material.SUGAR_CANE,
                Material.BAMBOO,
                Material.CACTUS,
                Material.LILY_PAD,
                Material.SPORE_BLOSSOM,
                Material.HANGING_ROOTS,
                Material.TWISTING_VINES,
                Material.AZALEA,
                Material.FLOWERING_AZALEA,
                Material.SCAFFOLDING,
                Material.WEEPING_VINES,
                Material.TORCHFLOWER,
                Material.CLOSED_EYEBLOSSOM,
                Material.OPEN_EYEBLOSSOM,
                Material.PALE_OAK_SAPLING
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the unusual placement of plants.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click a plant to place it in an unusual location.");
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
