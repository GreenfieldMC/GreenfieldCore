package net.greenfieldmc.core.armorstandeditor.services;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns session storage and lifecycle for the armor stand editor.
 *
 * This is a lightweight manager so ArmorStandInteraction can focus on
 * input->action logic.
 */
public final class ArmorStandSessionService {
    private final Map<UUID, ArmorStandSession> sessions = new ConcurrentHashMap<>();

    public void startSession(UUID playerUuid, ArmorStand armorStand) {
        if (playerUuid == null) throw new IllegalArgumentException("playerUuid");
        if (armorStand == null) throw new IllegalArgumentException("armorStand");
        sessions.put(playerUuid, new ArmorStandSession(armorStand));
    }

    public Optional<ArmorStandSession> getSession(UUID playerUuid) {
        if (playerUuid == null) return Optional.empty();
        return Optional.ofNullable(sessions.get(playerUuid));
    }

    public ArmorStandSession getSessionOrNull(UUID playerUuid) {
        if (playerUuid == null) return null;
        return sessions.get(playerUuid);
    }

    public boolean isEditing(UUID playerUuid) {
        return playerUuid != null && sessions.containsKey(playerUuid);
    }

    /**
     * Ends the session and reports changes to the player (if still online).
     */
    public Optional<ArmorStand> endSession(UUID playerUuid) {
        if (playerUuid == null) return Optional.empty();
        ArmorStandSession session = sessions.remove(playerUuid);
        if (session == null) return Optional.empty();

        Player p = Bukkit.getPlayer(playerUuid);

        return Optional.ofNullable(session.getActiveArmorStand());
    }

    public static class ArmorStandSession {
        // Track multiple armor stands by their UUIDs and store original snapshots per stand
        private final Map<UUID, ArmorStand> armorStands = new ConcurrentHashMap<>();
        private final Map<UUID, Map<String, Object>> originalSerialized = new ConcurrentHashMap<>();
        private UUID activeStandId;
        private ItemStack[] stashedHotbar;

        public ArmorStandSession(ArmorStand armorStand) {
        }

        public ArmorStand getActiveArmorStand() { return (activeStandId == null) ? null : armorStands.get(activeStandId); }

        public boolean hasStashedHotbar() { return this.stashedHotbar != null; }

        public boolean saveHotbarFromPlayer(Player player) {
            if (player == null) return false;
            if (this.stashedHotbar != null) return false; // already saved

            ItemStack[] stash = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItem(i);
                stash[i] = (item == null) ? null : item.clone();
                player.getInventory().setItem(i, null);
            }
            this.stashedHotbar = stash;
            player.updateInventory();
            return true;
        }

        public boolean recallHotbarFromPlayer(Player player) {
            if (player == null) return false;
            if (this.stashedHotbar == null) return false;

            for (int i = 0; i < 9; i++) {
                ItemStack item = this.stashedHotbar[i];
                player.getInventory().setItem(i, (item == null) ? null : item.clone());
            }
            this.stashedHotbar = null;
            player.updateInventory();
            return true;
        }
    }
}
