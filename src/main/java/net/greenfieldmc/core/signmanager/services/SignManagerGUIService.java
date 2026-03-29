package net.greenfieldmc.core.signmanager.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.signmanager.SignFlag;
import net.greenfieldmc.core.signmanager.SignManagerEntry;
import net.greenfieldmc.core.signmanager.SignManagerMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SignManagerGUIService extends ModuleService<ISignManagerGUIService> implements ISignManagerGUIService, Listener {

    private static final int GUI_SIZE = 54; // double chest
    private static final int ITEMS_PER_PAGE = 45; // slots 0-44 for sign items
    // Bottom row navigation slots
    private static final int SLOT_PREV_PAGE = 45;
    private static final int SLOT_FILTER_CONSTRUCTION = 46;
    private static final int SLOT_FILTER_ROAD = 47;
    private static final int SLOT_FILTER_RAIL = 48;
    private static final int SLOT_FILTER_TRANSIT = 49;
    private static final int SLOT_FILTER_MUNICIPAL = 50;
    private static final int SLOT_CLEAR_FILTER = 51;
    private static final int SLOT_PAGE_INFO = 52;
    private static final int SLOT_NEXT_PAGE = 53;

    private final ISignManagerService signManagerService;
    private final Map<UUID, GUISession> sessions = new HashMap<>();
    private final java.util.Set<UUID> rendering = new java.util.HashSet<>();

    public SignManagerGUIService(Plugin plugin, Module module, ISignManagerService signManagerService) {
        super(plugin, module);
        this.signManagerService = signManagerService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        for (var entry : sessions.entrySet()) {
            var player = Bukkit.getPlayer(entry.getKey());
            if (player != null) player.closeInventory();
        }
        sessions.clear();
    }

    @Override
    public void openGUI(Player player) {
        var entries = signManagerService.getAllEntries();
        var session = new GUISession(entries, null, null);
        sessions.put(player.getUniqueId(), session);
        renderPage(player, session);
    }

    @Override
    public void openGUIWithSearch(Player player, String query) {
        var entries = signManagerService.searchEntries(query);
        var session = new GUISession(entries, query, null);
        sessions.put(player.getUniqueId(), session);
        renderPage(player, session);
    }

    @Override
    public void openGUIWithFlag(Player player, SignFlag flag) {
        var entries = signManagerService.filterByFlag(flag);
        var session = new GUISession(entries, null, flag);
        sessions.put(player.getUniqueId(), session);
        renderPage(player, session);
    }

    @Override
    public boolean hasGUIOpen(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    private void renderPage(Player player, GUISession session) {
        var entries = session.entries;
        int startIndex = session.page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, entries.size());

        // Build base title
        Component title;
        if (session.searchQuery != null) {
            title = SignManagerMessages.GUI_TITLE_SEARCH.apply(session.searchQuery);
        } else if (session.activeFlag != null) {
            title = SignManagerMessages.GUI_TITLE_FLAG.apply(session.activeFlag.getDisplayName());
        } else {
            title = SignManagerMessages.GUI_TITLE;
        }

        // Append page range to title if there are entries on this page
        if (startIndex < entries.size()) {
            var firstName = entries.get(startIndex).getName().toLowerCase();
            var lastName = entries.get(endIndex - 1).getName().toLowerCase();
            var range = computeSignificantRange(firstName, lastName);
            title = title.append(Component.text(" (" + range + ")", NamedTextColor.GRAY));
        }

        var inventory = Bukkit.createInventory(null, GUI_SIZE, title);

        // Fill entry display items
        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(i - startIndex, entries.get(i).toDisplayItemStack());
        }

        // Fill bottom row with glass panes
        var filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        for (int i = ITEMS_PER_PAGE; i < GUI_SIZE; i++) {
            inventory.setItem(i, filler);
        }

        // Previous page button
        if (session.page > 0) {
            inventory.setItem(SLOT_PREV_PAGE, createGuiItem(Material.ARROW, Component.text("← Previous Page", NamedTextColor.GREEN)));
        }

        // Next page button
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) ITEMS_PER_PAGE));
        if (session.page < totalPages - 1) {
            inventory.setItem(SLOT_NEXT_PAGE, createGuiItem(Material.ARROW, Component.text("Next Page →", NamedTextColor.GREEN)));
        }

        // Page info
        inventory.setItem(SLOT_PAGE_INFO, createGuiItem(Material.PAPER,
                Component.text("Page " + (session.page + 1) + "/" + totalPages, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)));

        // Flag filter buttons
        setFlagFilterButton(inventory, SLOT_FILTER_CONSTRUCTION, SignFlag.CONSTRUCTION, session.activeFlag);
        setFlagFilterButton(inventory, SLOT_FILTER_ROAD, SignFlag.ROAD, session.activeFlag);
        setFlagFilterButton(inventory, SLOT_FILTER_RAIL, SignFlag.RAIL, session.activeFlag);
        setFlagFilterButton(inventory, SLOT_FILTER_TRANSIT, SignFlag.TRANSIT, session.activeFlag);
        setFlagFilterButton(inventory, SLOT_FILTER_MUNICIPAL, SignFlag.MUNICIPAL, session.activeFlag);

        // Clear filter button
        inventory.setItem(SLOT_CLEAR_FILTER, createGuiItem(Material.BARRIER,
                Component.text("Clear Filters", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));

        session.inventory = inventory;
        rendering.add(player.getUniqueId());
        player.openInventory(inventory);
        rendering.remove(player.getUniqueId());
    }

    private void setFlagFilterButton(Inventory inventory, int slot, SignFlag flag, @Nullable SignFlag activeFlag) {
        boolean isActive = flag == activeFlag;
        var material = isActive ? Material.LIME_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        var color = isActive ? NamedTextColor.GREEN : NamedTextColor.GRAY;
        inventory.setItem(slot, createGuiItem(material,
                Component.text(flag.getDisplayName(), color).decoration(TextDecoration.ITALIC, false)));
    }

    private ItemStack createGuiItem(Material material, Component name) {
        var item = new ItemStack(material, 1);
        var meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Computes the significant character range for two entry names.
     * Finds the shortest prefix that distinguishes the first name from the last name.
     * e.g. "railsign" and "roadsign" -> "ra-ro"
     *      "aaa" and "aaa" -> "aaa"
     *      "bridge" and "bus" -> "br-bu"
     */
    private String computeSignificantRange(String first, String last) {
        if (first.equals(last)) return first;

        // Find the index where the two names first differ
        int shared = 0;
        int minLen = Math.min(first.length(), last.length());
        while (shared < minLen && first.charAt(shared) == last.charAt(shared)) {
            shared++;
        }

        // Include up to one character past the divergence point (minimum 1 char total)
        int prefixLen = Math.min(shared + 1, minLen);

        // Ensure at least 1 character
        prefixLen = Math.max(prefixLen, 1);

        var firstPrefix = first.substring(0, Math.min(prefixLen, first.length()));
        var lastPrefix = last.substring(0, Math.min(prefixLen, last.length()));

        if (firstPrefix.equals(lastPrefix)) {
            // Edge case: one name is a prefix of the other, extend to distinguish
            firstPrefix = first.substring(0, Math.min(prefixLen + 1, first.length()));
            lastPrefix = last.substring(0, Math.min(prefixLen + 1, last.length()));
        }

        return firstPrefix + "-" + lastPrefix;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var session = sessions.get(player.getUniqueId());
        if (session == null || session.inventory == null) return;
        if (!event.getInventory().equals(session.inventory)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= GUI_SIZE) return;

        // Click on an entry item (slots 0-44)
        if (slot < ITEMS_PER_PAGE) {
            int index = session.page * ITEMS_PER_PAGE + slot;
            if (index < session.entries.size()) {
                var entry = session.entries.get(index);
                var items = entry.toGiveItemStacks();
                for (var item : items) {
                    player.getInventory().addItem(item);
                }
                if (entry.isGroup()) {
                    player.sendMessage(SignManagerMessages.MODULE.append(
                            Component.text("Gave you " + items.size() + " sign(s) from group \"" + entry.getName() + "\".", NamedTextColor.GRAY)));
                } else {
                    player.sendMessage(SignManagerMessages.MODULE.append(
                            Component.text("Gave you sign \"" + entry.getName() + "\".", NamedTextColor.GRAY)));
                }
            }
            return;
        }

        // Navigation clicks
        switch (slot) {
            case SLOT_PREV_PAGE -> {
                if (session.page > 0) {
                    session.page--;
                    renderPage(player, session);
                }
            }
            case SLOT_NEXT_PAGE -> {
                int totalPages = Math.max(1, (int) Math.ceil(session.entries.size() / (double) ITEMS_PER_PAGE));
                if (session.page < totalPages - 1) {
                    session.page++;
                    renderPage(player, session);
                }
            }
            case SLOT_FILTER_CONSTRUCTION -> applyFlagFilter(player, session, SignFlag.CONSTRUCTION);
            case SLOT_FILTER_ROAD -> applyFlagFilter(player, session, SignFlag.ROAD);
            case SLOT_FILTER_RAIL -> applyFlagFilter(player, session, SignFlag.RAIL);
            case SLOT_FILTER_TRANSIT -> applyFlagFilter(player, session, SignFlag.TRANSIT);
            case SLOT_FILTER_MUNICIPAL -> applyFlagFilter(player, session, SignFlag.MUNICIPAL);
            case SLOT_CLEAR_FILTER -> {
                session.activeFlag = null;
                session.searchQuery = null;
                session.entries = signManagerService.getAllEntries();
                session.page = 0;
                renderPage(player, session);
            }
        }
    }

    private void applyFlagFilter(Player player, GUISession session, SignFlag flag) {
        // Toggle: if already active, clear; otherwise set
        if (session.activeFlag == flag) {
            session.activeFlag = null;
            session.entries = session.searchQuery != null
                    ? signManagerService.searchEntries(session.searchQuery)
                    : signManagerService.getAllEntries();
        } else {
            session.activeFlag = flag;
            if (session.searchQuery != null) {
                // Combine search + flag
                session.entries = signManagerService.searchEntries(session.searchQuery).stream()
                        .filter(e -> e.getFlag() == flag)
                        .toList();
            } else {
                session.entries = signManagerService.filterByFlag(flag);
            }
        }
        session.page = 0;
        renderPage(player, session);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (!rendering.contains(player.getUniqueId())) {
                sessions.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Tracks the state of a player's open Sign Manager GUI.
     */
    private static class GUISession {
        List<SignManagerEntry> entries;
        @Nullable String searchQuery;
        @Nullable SignFlag activeFlag;
        int page;
        @Nullable Inventory inventory;

        GUISession(List<SignManagerEntry> entries, @Nullable String searchQuery, @Nullable SignFlag activeFlag) {
            this.entries = entries;
            this.searchQuery = searchQuery;
            this.activeFlag = activeFlag;
            this.page = 0;
        }
    }
}

