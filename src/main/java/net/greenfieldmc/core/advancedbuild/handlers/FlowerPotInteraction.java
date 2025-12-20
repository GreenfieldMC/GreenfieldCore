// src/main/java/net/greenfieldmc/core/advancedbuild/handlers/FlowerPotInteraction.java
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
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlowerPotInteraction extends InteractionHandler {

    private final Map<UUID, FlowerPotSession> sessions = new HashMap<>();

    public FlowerPotInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
            var block = event.getClickedBlock();
            return block != null && isAnyFlowerPot(block.getType());
        }, Material.FLOWER_POT);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Plant or remove items in flower pots without block updates. Shift-click to place with last plant.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Right-click with plantable to plant, empty hand to remove. Shift-click to place with last plant.").color(NamedTextColor.GRAY);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        UUID uuid = event.getPlayer().getUniqueId();
        ItemStack handItem = event.getItem();
        boolean isEmptyHand = handItem == null || handItem.getType() == Material.AIR;

        if (event.getPlayer().isSneaking()) {
            // If holding a flower pot in main hand, place the last potted item (or an empty pot if none)
            if (handItem != null && handItem.getType() == Material.FLOWER_POT) {
                Material last = getSession(uuid).getLastPotted();
                assert block != null;
                Block target = block.getRelative(0, 1, 0);
                target.setType(last != null ? last : Material.FLOWER_POT, false); // place on top (no block update)
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                return;
            }
            Material plantType = handItem.getType();
            Material pottedType = getPottedType(plantType);
            if (pottedType != null) {
                getSession(uuid).setLastPotted(pottedType);
                assert block != null;
                block.setType(pottedType, false); // no block update
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
            }
            return;
        }

        if (isEmptyHand) {
            // Remove plant from pot
            getSession(uuid).setLastPotted(null);
            assert block != null;
            block.setType(Material.FLOWER_POT, false);
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        // Plant item if possible
        Material plantType = handItem.getType();
        Material pottedType = getPottedType(plantType);
        if (pottedType != null) {
            getSession(uuid).setLastPotted(pottedType);
            assert block != null;
            block.setType(pottedType, false); // no block update
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    private static boolean isAnyFlowerPot(Material mat) {
        return mat == Material.FLOWER_POT || mat.name().startsWith("POTTED_");
    }

    private static Material getPottedType(Material plant) {
        // Map plantable items to their potted block type
        return switch (plant) {
            case DANDELION -> Material.POTTED_DANDELION;
            case POPPY -> Material.POTTED_POPPY;
            case BLUE_ORCHID -> Material.POTTED_BLUE_ORCHID;
            case ALLIUM -> Material.POTTED_ALLIUM;
            case AZURE_BLUET -> Material.POTTED_AZURE_BLUET;
            case RED_TULIP -> Material.POTTED_RED_TULIP;
            case ORANGE_TULIP -> Material.POTTED_ORANGE_TULIP;
            case WHITE_TULIP -> Material.POTTED_WHITE_TULIP;
            case PINK_TULIP -> Material.POTTED_PINK_TULIP;
            case OXEYE_DAISY -> Material.POTTED_OXEYE_DAISY;
            case CORNFLOWER -> Material.POTTED_CORNFLOWER;
            case LILY_OF_THE_VALLEY -> Material.POTTED_LILY_OF_THE_VALLEY;
            case WITHER_ROSE -> Material.POTTED_WITHER_ROSE;
            case CACTUS -> Material.POTTED_CACTUS;
            case DEAD_BUSH -> Material.POTTED_DEAD_BUSH;
            case FERN -> Material.POTTED_FERN;
            case BAMBOO -> Material.POTTED_BAMBOO;
            case BROWN_MUSHROOM -> Material.POTTED_BROWN_MUSHROOM;
            case RED_MUSHROOM -> Material.POTTED_RED_MUSHROOM;
            case OAK_SAPLING -> Material.POTTED_OAK_SAPLING;
            case SPRUCE_SAPLING -> Material.POTTED_SPRUCE_SAPLING;
            case BIRCH_SAPLING -> Material.POTTED_BIRCH_SAPLING;
            case JUNGLE_SAPLING -> Material.POTTED_JUNGLE_SAPLING;
            case ACACIA_SAPLING -> Material.POTTED_ACACIA_SAPLING;
            case DARK_OAK_SAPLING -> Material.POTTED_DARK_OAK_SAPLING;
            // Add more mappings as needed
            default -> null;
        };
    }

    private FlowerPotSession getSession(UUID uuid) {
        return sessions.computeIfAbsent(uuid, k -> new FlowerPotSession());
    }

    public static class FlowerPotSession {
        private Material lastPotted;

        public Material getLastPotted() { return lastPotted; }
        public void setLastPotted(Material lastPotted) { this.lastPotted = lastPotted; }
    }
}