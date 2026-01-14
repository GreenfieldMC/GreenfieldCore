package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class Size extends ButtonHandler {

    public Size(ArmorStand armorStand) {
        super(sizeItem(armorStand));
    }

    public void size(ArmorStand stand) {
        if (stand == null) return;
        stand.setSmall(!stand.isSmall());
    }

    // Overload that accepts player/session/hotbarService and refreshes the hotbar after modification
    public void size(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        size(stand);
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack sizeItem(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Toggles baby size")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent(); if (cmd != null) stringList.addAll(cmd.getStrings());
        if (armorStand != null && armorStand.isSmall()) {
            meta.displayName(Component.text("Make Baby"));
            stringList.add("baby");
        } else {
            meta.displayName(Component.text("Make Adult"));
            stringList.add("adult");
        }
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }
}
