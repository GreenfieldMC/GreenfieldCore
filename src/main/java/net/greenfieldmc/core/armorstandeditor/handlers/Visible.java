package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class Visible extends ButtonHandler {

    public Visible(ArmorStand armorStand) {
        super(visibleItem(armorStand));
    }

    public boolean visible(ArmorStand stand, Player p) {
        if (stand == null) return false;
        boolean newVisible = !stand.isVisible();
        // If player is attempting to make the stand invisible, ensure it has at least one visible feature
        if (!newVisible) {
            if (!hasAtLeastOneVisibleFeature(stand)) {
                if (p != null) p.sendMessage(Component.text("[ase] ").color(NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("LagGuard ").color(NamedTextColor.GOLD))
                        .append(Component.text("Cannot make armor stand invisible: it would be completely invisible. Add armor, arms, or a base plate first.").color(NamedTextColor.GRAY)));
                return false;
            }
        }
        stand.setVisible(newVisible);
        return true;
    }

    public void visible(ArmorStand stand, int slotInt, Player p, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        boolean changed = visible(stand, p);
        if (changed && p != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(p, stand, new int[]{slotInt});
        }
    }

    public static ItemStack visibleItem(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Toggle the visibility of the armor stand.")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent();
        if (cmd != null) stringList.addAll(cmd.getStrings());
        if (armorStand != null && armorStand.isVisible()) {
            meta.displayName(Component.text("Hide"));
            stringList.add("Show");
        } else {
            meta.displayName(Component.text("Show"));
            stringList.add("Hide");
        }
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }

    private static boolean hasAtLeastOneVisibleFeature(ArmorStand stand) {
        if (stand == null) return false;

        String name = stand.getCustomName();
        if (name != null && !name.trim().isEmpty()) return true;

        org.bukkit.inventory.EntityEquipment eq = stand.getEquipment();
        if (eq != null) {
            ItemStack[] armor = eq.getArmorContents();
            if (armor != null) {
                for (ItemStack it : armor) {
                    if (it != null && it.getType() != Material.AIR) return true;
                }
            }
            ItemStack main = eq.getItemInMainHand();
            if (main != null && main.getType() != Material.AIR) return true;
            ItemStack off = eq.getItemInOffHand();
            if (off != null && off.getType() != Material.AIR) return true;
        }

        return false;
    }
}
