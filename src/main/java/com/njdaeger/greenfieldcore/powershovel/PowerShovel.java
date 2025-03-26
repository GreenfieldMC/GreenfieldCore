package com.njdaeger.greenfieldcore.powershovel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowerShovel extends ItemStack {

    public PowerShovel() {
        super(Material.IRON_SHOVEL);
        setAmount(1);
        ItemMeta meta = getItemMeta();
        if (meta == null) throw new IllegalStateException("ItemMeta was null. Please contact the developer.");
        meta.displayName(Component.text("Power Shovel", NamedTextColor.BLUE, TextDecoration.BOLD));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        setItemMeta(meta);
        setAmount(1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ItemStack) {
            ItemStack stack = (ItemStack) obj;
            ItemMeta objMeta = stack.getItemMeta();
            ItemMeta meta = getItemMeta();
            return stack.getType() == getType() &&
                    objMeta != null &&
                    meta != null &&
                    objMeta.hasEnchant(Enchantment.UNBREAKING) &&
                    objMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE) &&
                    objMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                    objMeta.isUnbreakable();
        }
        return false;
    }

}
