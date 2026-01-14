package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.storage.ArmorStandPosePreset;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Pose extends ButtonHandler {

    public Pose() {
        super(
                poseItem(),
                ArmorStandPosePreset.DEFAULT.toItemStack(),
                ArmorStandPosePreset.HERO.toItemStack(),
                ArmorStandPosePreset.HOLD.toItemStack(),
                ArmorStandPosePreset.LEGLESS.toItemStack(),
                ArmorStandPosePreset.POINT_LEFT.toItemStack(),
                ArmorStandPosePreset.POINT_RIGHT.toItemStack(),
                ArmorStandPosePreset.SIT.toItemStack(),
                ArmorStandPosePreset.T_POSE.toItemStack(),
                ArmorStandPosePreset.WALK.toItemStack());
    }

    /**
     * Handle a PlayerInteractAtEntity event for armor-stand pose tooling.
     * - If the player is holding the pose selector (poseItem()), save their hotbar if not already saved
     *   and replace their hotbar with the available pose preset items.
     * - If the player is holding one of the preset items, apply that preset to the clicked armor stand.
     *
     * This method is intentionally minimal and defensive; callers should pass the session and hotbar services.
     */
    public void pose(ArmorStand clicked, Player player, ItemStack heldItem,
                                      ArmorStandSessionService sessionService,
                                      ArmorStandHotbarService hotbarService,
                                      Plugin plugin) {
        if (player == null || clicked == null || heldItem == null) return;
        if (sessionService == null || hotbarService == null) return;

        if (isSameItem(heldItem, poseItem())) {
            // ensure a session exists for this player (create if necessary)
            var session = sessionService.getSessionOrNull(player.getUniqueId());
            if (session == null) {
                sessionService.startSession(player.getUniqueId(), clicked);
                session = sessionService.getSessionOrNull(player.getUniqueId());
            }

            // If player doesn't have a stashed hotbar, save it first
            if (session != null && !session.hasStashedHotbar()) {
                hotbarService.savePlayerHotbar(player, session);
            }

            // Replace player's hotbar (slots 0..8) with pose preset items
            ItemStack[] poseItems = buildPoseHotbarItems();
            for (int i = 0; i < 9 && i < poseItems.length; i++) {
                player.getInventory().setItem(i, (poseItems[i] == null) ? null : poseItems[i].clone());
            }
            return;
        }

        // Otherwise, if the held item matches one of the preset items, apply it
        ItemMeta meta = heldItem.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String display = meta.getDisplayName();
            for (ArmorStandPosePreset preset : ArmorStandPosePreset.values()) {
                String name = preset.getDisplayName();
                if (name != null && name.equals(display)) {
                    preset.applyTo(clicked);
                    NamespacedKey key = new NamespacedKey(plugin, "pose_preset");
                    clicked.getPersistentDataContainer().set(key, PersistentDataType.STRING, preset.getKey());
                }
            }
        }
    }

    // Build an array of up to 9 ItemStacks representing the pose presets (skips DEFAULT)
    private static ItemStack[] buildPoseHotbarItems() {
        ItemStack[] items = new ItemStack[9];
        int idx = 0;
        for (ArmorStandPosePreset p : ArmorStandPosePreset.values()) {
            if (p == ArmorStandPosePreset.DEFAULT) continue;
            if (idx >= 9) break;
            items[idx++] = p.toItemStack();
        }
        return items;
    }

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return a.isSimilar(b);
    }

    public static ItemStack poseItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Pose the armor stand.")));
        List<String> stringList = new ArrayList<>(meta.getCustomModelDataComponent().getStrings());
        meta.displayName(Component.text("Pose")
                .decorate(TextDecoration.BOLD));
        stringList.add("pose");
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) {
            cmdc.setStrings(stringList);
            meta.setCustomModelDataComponent(cmdc);
        }
        item.setItemMeta(meta);
        return item;
    }
}
