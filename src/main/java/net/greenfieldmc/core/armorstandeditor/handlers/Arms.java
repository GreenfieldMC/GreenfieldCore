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

public class Arms extends ButtonHandler {

    public Arms(ArmorStand armorStand) {
        super(armsItem(armorStand));
    }

    public void arms(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        if (stand == null) return;
        stand.setArms(!stand.hasArms());
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack armsItem(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Toggles arms.")));
        List<String> stringList = new ArrayList<>();
        if (meta != null) {
            var cmd = meta.getCustomModelDataComponent();
            if (cmd != null) stringList.addAll(cmd.getStrings());
        }
        if (armorStand != null && armorStand.hasArms()) {
            meta.displayName(Component.text("Remove Arms"));
            stringList.add("arms");
        } else {
            meta.displayName(Component.text("Add Arms"));
            stringList.add("no_arms");
        }


        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) {
            cmdc.setStrings(stringList);
            meta.setCustomModelDataComponent(cmdc);
        }
        item.setItemMeta(meta);
        return item;
    }
}
