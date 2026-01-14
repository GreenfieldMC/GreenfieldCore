package net.greenfieldmc.core.armorstandeditor.handlers;

import net.greenfieldmc.core.armorstandeditor.ButtonHandler;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Slot extends ButtonHandler {

    private static ArmorStandHotbarService hotbarService;

    public Slot(ArmorStandHotbarService hotbarService, ArmorStand armorStand) {
        super(slotItem());
        Slot.hotbarService = hotbarService;
    }

    // Equipment mapping inside the 54-slot GUI (user-specified indices)
    // Head in 14, Chest in 23, Main in 24, Off in 22, Legs in 32, Boots in 41
    private static final int SLOT_HEAD = 14;
    private static final int SLOT_CHEST = 23;
    private static final int SLOT_MAIN = 24;
    private static final int SLOT_OFF = 22;
    private static final int SLOT_LEGS = 32;
    private static final int SLOT_BOOTS = 41;

    /**
     * Open a per-player slot editor inventory (54 slots) that copies the player's
     * first 54 inventory slots and exposes equipment slots at fixed indices.
     * Any item placed into an equipment slot will be applied to the ArmorStand
     * immediately (a clone with amount 1 is applied). When the inventory is closed,
     * contents are copied back to the player's first 27 inventory slots.
     */
    public void slot( ArmorStand stand, int slotInt, Player player, Plugin plugin) {

        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(stand, "armorStand");
        Objects.requireNonNull(plugin, "plugin");

        String title = (stand.getCustomName() != null && !stand.getCustomName().trim().isEmpty())
                ? stand.getCustomName()
                : "ArmorStand " + stand.getUniqueId();

        SlotEditorHolder holder = new SlotEditorHolder(stand.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // Populate reserved equipment slots with the armor stand's current equipment (clone)
        var eq = stand.getEquipment();
        if (eq != null) {
            // If stand slot empty, show a sensible default so the user can see/apply it.
            inv.setItem(SLOT_HEAD, firstNonNull(cloneOne(eq.getHelmet()), defaultForSlot(SLOT_HEAD)));
            inv.setItem(SLOT_CHEST, firstNonNull(cloneOne(eq.getChestplate()), defaultForSlot(SLOT_CHEST)));
            inv.setItem(SLOT_MAIN, firstNonNull(cloneOne(eq.getItemInMainHand()), defaultForSlot(SLOT_MAIN)));
            inv.setItem(SLOT_OFF, firstNonNull(cloneOne(eq.getItemInOffHand()), defaultForSlot(SLOT_OFF)));
            inv.setItem(SLOT_LEGS, firstNonNull(cloneOne(eq.getLeggings()), defaultForSlot(SLOT_LEGS)));
            inv.setItem(SLOT_BOOTS, firstNonNull(cloneOne(eq.getBoots()), defaultForSlot(SLOT_BOOTS)));
        }

        // Register a per-inventory listener that processes clicks and close
        SlotEditorListener listener = new SlotEditorListener(inv, stand.getUniqueId(), player, slotInt);
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);

        // Open the inventory for the player
        player.openInventory(inv);
    }

    private static ItemStack cloneOne(ItemStack in) {
        if (in == null) return null;
        ItemStack c = in.clone();
        c.setAmount(1);
        return c;
    }

    private static final class SlotEditorHolder implements InventoryHolder {
        private final UUID standId;
        private Inventory inventory;

        public SlotEditorHolder(UUID standId) { this.standId = standId; }
        public UUID getStandId() { return standId; }
        public void setInventory(Inventory inv) { this.inventory = inv; }
        @Override public Inventory getInventory() { return inventory; }
    }

    private static final class SlotEditorListener implements Listener {
        private final Inventory top;
        private final UUID standId;
        private final Player player;
        private final int slotInt;

        SlotEditorListener(Inventory top, UUID standId, Player player, int slotInt) {
            this.top = top;
            this.standId = standId;
            this.player = player;
            this.slotInt = slotInt;
        }

        private ArmorStand resolveStand() {
            var world = player.getWorld();
            for (var e : world.getEntities()) {
                if (e instanceof ArmorStand as && as.getUniqueId().equals(standId)) return as;
            }
            return null;
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryClick(InventoryClickEvent e) {
            if (!e.getView().getTopInventory().equals(top)) return;
            if (!(e.getWhoClicked() instanceof Player p)) return;
            if (!p.getUniqueId().equals(player.getUniqueId())) return; // only the opener
            if (e.getClickedInventory() == null || !e.getClickedInventory().equals(top)) return;

            int slot = e.getSlot();
            if (slot == SLOT_HEAD || slot == SLOT_CHEST || slot == SLOT_MAIN || slot == SLOT_OFF || slot == SLOT_LEGS || slot == SLOT_BOOTS) {
                e.setCancelled(true);
                ArmorStand stand = resolveStand();
                if (stand == null) {
                    player.sendMessage(Component.text("ArmorStand no longer available.").decorate(TextDecoration.BOLD));
                    return;
                }

                ItemStack cursor = e.getCursor();
                try {
                    var eq = stand.getEquipment();
                    if (slot == SLOT_HEAD) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setHelmet(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            // if cleared, apply default instead of leaving empty
                            ItemStack def = defaultForSlot(SLOT_HEAD);
                            eq.setHelmet(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else if (slot == SLOT_CHEST) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setChestplate(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            ItemStack def = defaultForSlot(SLOT_CHEST);
                            eq.setChestplate(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else if (slot == SLOT_MAIN) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setItemInMainHand(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            ItemStack def = defaultForSlot(SLOT_MAIN);
                            eq.setItemInMainHand(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else if (slot == SLOT_OFF) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setItemInOffHand(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            ItemStack def = defaultForSlot(SLOT_OFF);
                            eq.setItemInOffHand(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else if (slot == SLOT_LEGS) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setLeggings(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            ItemStack def = defaultForSlot(SLOT_LEGS);
                            eq.setLeggings(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else if (slot == SLOT_BOOTS) {
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            eq.setBoots(cursor.clone());
                            top.setItem(slot, cloneOne(cursor));
                        } else {
                            ItemStack def = defaultForSlot(SLOT_BOOTS);
                            eq.setBoots(def.clone());
                            top.setItem(slot, cloneOne(def));
                        }
                    } else {
                        player.sendMessage(Component.text("Invalid equipment slot index: " + slot));
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    player.sendMessage(Component.text("Failed to apply equipment: " + ex.getMessage()));
                }
            }
            // otherwise allow default manipulation within the GUI (moving items around)
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (!e.getView().getTopInventory().equals(top)) return;
            if (!(e.getPlayer() instanceof Player p)) return;
            if (!p.getUniqueId().equals(player.getUniqueId())) return;
            ArmorStand stand = resolveStand();
            if (stand == null) return;
            hotbarService.loadEditingHotbar(player, stand, new int[]{slotInt});
            // unregister this listener
            HandlerList.unregisterAll(this);
        }
    }

    public static ItemStack slotItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Opens GUI to edit armor stand equipment slots.")));
        List<String> stringList = new ArrayList<>();
        var cmd = meta.getCustomModelDataComponent();
        if (cmd != null) stringList.addAll(cmd.getStrings());
        meta.displayName(Component.text("Pose Slot")
                .decorate(TextDecoration.BOLD));
        stringList.add("slot");
        CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
        if (cmdc != null) {
            cmdc.setStrings(stringList);
            meta.setCustomModelDataComponent(cmdc);
        }
        item.setItemMeta(meta);
        return item;
    }

    // Helper: return the provided item if non-null, otherwise the default fallback
    private static ItemStack firstNonNull(ItemStack a, ItemStack b) {
        return (a != null) ? a : (b != null ? b.clone() : null);
    }

    // Return default items for equipment slots (iron armor, iron sword, shield)
    private static ItemStack defaultForSlot(int slot) {
        switch (slot) {
            case SLOT_HEAD: return new ItemStack(Material.IRON_HELMET);
            case SLOT_CHEST: return new ItemStack(Material.IRON_CHESTPLATE);
            case SLOT_LEGS: return new ItemStack(Material.IRON_LEGGINGS);
            case SLOT_BOOTS: return new ItemStack(Material.IRON_BOOTS);
            case SLOT_MAIN: return new ItemStack(Material.IRON_SWORD);
            case SLOT_OFF: return new ItemStack(Material.SHIELD);
            default: return new ItemStack(Material.AIR);
        }
    }
}
