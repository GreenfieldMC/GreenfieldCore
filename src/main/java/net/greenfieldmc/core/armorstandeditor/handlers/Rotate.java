package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class Rotate extends ButtonHandler {

    public Rotate(ArmorStand armorStand) {
        super(rotateItem());
    }

    public void rotate(ArmorStand stand) {
        if (stand == null) return;
        Location loc = stand.getLocation();
        float newYaw = (loc.getYaw() + 45f) % 360f;
        loc.setYaw(newYaw);
        stand.teleport(loc);
    }

    public void rotate(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        rotate(stand);
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack rotateItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Rotates the armor stand in 45 degree increments.")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent(); if (cmd != null) stringList.addAll(cmd.getStrings());
        meta.displayName(Component.text("Rotate"));
        stringList.add("rotate");
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }
}
