package com.njdaeger.greenfieldcore.powershovel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowerShovel extends ItemStack {

    public PowerShovel() {
        setAmount(1);
        setType(Material.IRON_SHOVEL);
        ItemMeta meta = getItemMeta();
        if (meta == null) throw new IllegalStateException("ItemMeta was null. Please contact the developer.");
        meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Power Shovel");
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        setItemMeta(meta);
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
                    objMeta.getDisplayName().equals(meta.getDisplayName()) &&
                    objMeta.hasEnchant(Enchantment.UNBREAKING) &&
                    objMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE) &&
                    objMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                    objMeta.isUnbreakable();
        }
        return false;
    }

}
