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

public class Copy extends ButtonHandler {

    public Copy(ArmorStand armorStand) {
        super(copyItem());
    }

    public void copy(ArmorStand stand, Player player, ArmorStandSessionService.ArmorStandSession session) {
        // implement copying behavior if desired -> give player an item representing the armor stand
    }

    public void copy(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        copy(stand, player, session);
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack copyItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Gives you a copy of the armor stand as an item.")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent();
        if (cmd != null) stringList.addAll(cmd.getStrings());
        meta.displayName(Component.text("Copy"));
        stringList.add("copy");
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }
}
