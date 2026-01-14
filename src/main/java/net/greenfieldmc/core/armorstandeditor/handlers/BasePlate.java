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

public class BasePlate extends ButtonHandler {

    public BasePlate(ArmorStand armorStand) {
        super(basePlateItem(armorStand));
    }

    // Backwards-compatible overload that also accepts the hotbar service
    public BasePlate(ArmorStandHotbarService hotbarService, ArmorStand armorStand) {
        super(basePlateItem(armorStand));
    }

    public void toggleBasePlate(ArmorStand stand, int slotInt, Player player, ArmorStandSessionService.ArmorStandSession session, ArmorStandHotbarService hotbarService) {
        if (stand == null) return;
        stand.setBasePlate(!stand.hasBasePlate());
        if (player != null && hotbarService != null) {
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
        }
    }

    public static ItemStack basePlateItem(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Toggle base plate.")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent();
        if (cmd != null) stringList.addAll(cmd.getStrings());
        if (armorStand != null && armorStand.hasBasePlate()) {
            meta.displayName(Component.text("Show Base Plate"));
            stringList.add("no_base_plate");
        } else {
            meta.displayName(Component.text("Hide Base Plate"));
            stringList.add("base_plate");
        }
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) { cmdc.setStrings(stringList); meta.setCustomModelDataComponent(cmdc); }
        item.setItemMeta(meta);
        return item;
    }
}