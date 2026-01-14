package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lock extends ButtonHandler {

    public Lock(ArmorStand armorStand) {
        super(lockItem(armorStand));
    }

    public void lock(ArmorStand stand) {
        if (stand == null) return;
        boolean anyDisabled = Arrays.stream(org.bukkit.inventory.EquipmentSlot.values()).anyMatch(stand::isSlotDisabled);
        if (anyDisabled) {
            stand.removeDisabledSlots(org.bukkit.inventory.EquipmentSlot.values());
            stand.setGravity(true);
            stand.setInvulnerable(false);
        } else {
            stand.addDisabledSlots(org.bukkit.inventory.EquipmentSlot.values());
            stand.setGravity(false);
            stand.setInvulnerable(true);
        }
    }

    public void lock(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        lock(stand);
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack lockItem(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Toggles ability to place, or remove any equipment on the armor stand,"),
                Component.text("Toggles Invulnerable"),
                Component.text("Toggles Gravity")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent(); if (cmd != null) stringList.addAll(cmd.getStrings());
        if (armorStand != null && Arrays.stream(EquipmentSlot.values()).anyMatch(armorStand::isSlotDisabled)) {
            meta.displayName(Component.text("Unlock")
                    .decorate(TextDecoration.BOLD));
            stringList.add("lock");
        } else {
            meta.displayName(Component.text("Lock")
                    .decorate(TextDecoration.BOLD));
            stringList.add("unlock");
        }
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }
}
