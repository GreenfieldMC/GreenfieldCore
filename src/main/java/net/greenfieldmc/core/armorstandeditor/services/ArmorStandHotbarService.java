package net.greenfieldmc.core.armorstandeditor.services;

import net.greenfieldmc.core.armorstandeditor.handlers.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class ArmorStandHotbarService {

    public boolean savePlayerHotbar(Player player, ArmorStandSessionService.ArmorStandSession session) {
        if (player == null || session == null) return false;
        return session.saveHotbarFromPlayer(player);
    }

    public boolean restorePlayerHotbar(Player player, ArmorStandSessionService.ArmorStandSession session) {
        if (player == null || session == null) return false;
        return session.recallHotbarFromPlayer(player);
    }

    public boolean loadEditingHotbar(Player player, ArmorStand stand, int[] indices) {
        ItemStack[] items = {
                //1
                Lock.lockItem(stand),
                //2
                BasePlate.basePlateItem(stand),
                //3
                Arms.armsItem(stand),
                //4
                Visible.visibleItem(stand),
                //5
                Pose.poseItem(),
                //6
                Rotate.rotateItem(),
                //7
                Size.sizeItem(stand),
                //8
                Slot.slotItem(),
                //9
                Copy.copyItem()
        };

        for (int idx : indices) {
            if (idx < 0 || idx > 8) continue;
            ItemStack src = items[idx];
            player.getInventory().setItem(idx, src == null ? null : src.clone());
        }

        player.updateInventory();

        return true;
    }
}
