package net.greenfieldmc.core.templates.paginators;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A double-chest GUI paginator for browsing templates.
 *
 * <p>Layout:
 * <ul>
 *   <li><b>Top inventory (chest, 54 slots)</b> — template items (rows 1-5) + navigation row (row 6)</li>
 *   <li><b>Player main inventory (slots 9-35)</b> — 27 attribute-flag filter items (3 rows)</li>
 *   <li><b>Player hotbar (slots 0-8)</b> — flag navigation: prev(0), clear(1), info(4), next(8)</li>
 * </ul>
 *
 * <p>The player's inventory is cached on open and fully restored on close.
 */
public class TemplatePaginator implements Listener {

    // ---- Chest (top) layout ----
    private static final int CHEST_SIZE           = 54;
    private static final int ITEMS_PER_PAGE       = 45;  // rows 1-5
    private static final int SLOT_PREV            = 45;
    private static final int SLOT_PASTE_AIR       = 47;  // "paste ignore air" toggle
    private static final int SLOT_INFO            = 49;
    private static final int SLOT_RANDOM_ROTATION = 51;  // "random rotation" toggle
    private static final int SLOT_NEXT            = 53;

    // ---- Player inventory layout ----
    // Main inventory rows 1-3 = player inventory slots 9-35 (27 slots) → flag items
    private static final int FLAGS_PER_PAGE  = 27;
    private static final int FLAG_SLOT_START = 9;   // first player-inv slot used for flags
    private static final int FLAG_SLOT_END   = 35;  // last  player-inv slot used for flags
    // Hotbar (player inventory slots 0-8) → flag navigation
    private static final int HOTBAR_PREV     = 0;
    private static final int HOTBAR_CLEAR    = 1;
    private static final int HOTBAR_INFO     = 4;
    private static final int HOTBAR_NEXT     = 8;

    private final Plugin plugin;
    private final ITemplateService templateService;
    private final ITemplateViewerService viewerService;
    private final Map<UUID, PaginatorSession> sessions = new HashMap<>();

    public TemplatePaginator(Plugin plugin, ITemplateService templateService, ITemplateViewerService viewerService) {
        this.plugin = plugin;
        this.templateService = templateService;
        this.viewerService = viewerService;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // =========================================================
    //  Public API
    // =========================================================

    /**
     * Open the template GUI for a player.
     *
     * @param player       the player
     * @param templates    the full template list (may be pre-filtered by the caller)
     * @param mode         LIST or BRUSH_MODIFY
     * @param brush        brush context (only needed in BRUSH_MODIFY mode)
     * @param initialFilter text label shown in the info item (does not affect display)
     * @param page         starting chest-page (1-based)
     * @param onSelect     unused callback slot (reserved for future use)
     */
    public void open(Player player, List<Template> templates, TemplatePaginatorMode mode,
                     @Nullable TemplateBrush brush, @Nullable String initialFilter,
                     int page, @Nullable BiConsumer<Player, Template> onSelect) {

        var base = new ArrayList<>(templates);
        base.sort(Comparator.comparing(t -> t.getTemplateName().toLowerCase()));

        // Collect all unique attributes that exist in the base list → become flag items
        var allFlags = base.stream()
                .flatMap(t -> t.getAttributes().stream())
                .distinct().sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        int totalFlagPages = Math.max(1, (int) Math.ceil(allFlags.size() / (double) FLAGS_PER_PAGE));
        var filtered       = new ArrayList<>(base);
        int totalPages     = Math.max(1, (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE));
        page               = Math.max(1, Math.min(page, totalPages));

        // If a session already exists for this player (e.g. from a brush-command reopen),
        // preserve the original inventory cache so we restore the real items on close.
        var existingSession = sessions.get(player.getUniqueId());
        var cachedInv = (existingSession != null)
                ? existingSession.cachedInventory
                : player.getInventory().getContents().clone();

        // Preserve active flags across brush reopens
        Set<String> activeFlags = (existingSession != null)
                ? existingSession.activeFlags
                : new LinkedHashSet<>();
        int flagPage = (existingSession != null) ? existingSession.flagPage : 0;
        flagPage = Math.min(flagPage, totalFlagPages - 1);

        // Read paste-ignore-air preference from the persistent player session (default true)
        boolean pasteIgnoreAir = templateService.isPasteIgnoreAir(player.getUniqueId());

        // Preserve random rotation state across reopens (default false)
        boolean randomRotation = (existingSession != null) && existingSession.randomRotation;

        var session = new PaginatorSession(base, filtered, mode, brush, initialFilter,
                page, totalPages, allFlags, activeFlags, flagPage, totalFlagPages,
                cachedInv, onSelect, pasteIgnoreAir, randomRotation);

        // Re-apply any active flags to the new base list
        if (!activeFlags.isEmpty()) rebuildFilter(session);

        sessions.put(player.getUniqueId(), session);

        // Populate flag area in player inventory BEFORE opening the chest
        populatePlayerInventory(player, session);
        player.openInventory(buildChestInventory(session));
    }

    // =========================================================
    //  Chest inventory builder
    // =========================================================

    private Inventory buildChestInventory(PaginatorSession session) {
        var inv = Bukkit.createInventory(null, CHEST_SIZE,
                Component.text(computeTitle(session.filteredTemplates, session.page), NamedTextColor.DARK_PURPLE));
        fillChestContents(inv, session);
        return inv;
    }

    /** Update the chest inventory in-place — avoids firing InventoryCloseEvent during navigation. */
    private void fillChestContents(Inventory inv, PaginatorSession session) {
        inv.clear();
        var sorted = session.filteredTemplates;
        int start = (session.page - 1) * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, sorted.size());
        for (int i = start; i < end; i++) inv.setItem(i - start, createTemplateItem(sorted.get(i), session));

        var filler = makeFiller();
        for (int i = 45; i < 54; i++) inv.setItem(i, filler);
        inv.setItem(SLOT_PREV,            session.page > 1              ? makePrevButton()                     : filler);
        inv.setItem(SLOT_PASTE_AIR,       makePasteAirToggle(session.pasteIgnoreAir));
        inv.setItem(SLOT_INFO,            makeInfoItem(session));
        inv.setItem(SLOT_RANDOM_ROTATION, session.mode == TemplatePaginatorMode.BRUSH ? makeRandomRotation(session.randomRotation) : filler);
        inv.setItem(SLOT_NEXT,            session.page < session.totalPages ? makeNextButton()                : filler);
    }

    // =========================================================
    //  Player inventory (flags panel)
    // =========================================================

    private void populatePlayerInventory(Player player, PaginatorSession session) {
        var inv = player.getInventory();

        // Clear hotbar and the 3 main rows we use
        for (int i = 0; i < 9; i++) inv.setItem(i, null);
        for (int i = FLAG_SLOT_START; i <= FLAG_SLOT_END; i++) inv.setItem(i, null);

        // Flag items (main inventory rows 1-3, player slots 9-35)
        var allFlags = session.allFlags;
        int flagStart = session.flagPage * FLAGS_PER_PAGE;
        int flagEnd   = Math.min(flagStart + FLAGS_PER_PAGE, allFlags.size());
        for (int i = flagStart; i < flagEnd; i++) {
            String flag = allFlags.get(i);
            inv.setItem(FLAG_SLOT_START + (i - flagStart),
                    makeFlagItem(flag, session.activeFlags.contains(flag)));
        }

        // Hotbar navigation
        inv.setItem(HOTBAR_PREV,  session.flagPage > 0                              ? makeFlagNavButton(false) : null);
        inv.setItem(HOTBAR_CLEAR, !session.activeFlags.isEmpty()                    ? makeClearFlagsButton()   : null);
        inv.setItem(HOTBAR_INFO,  makeFlagInfoItem(session));
        inv.setItem(HOTBAR_NEXT,  session.flagPage < session.totalFlagPages - 1     ? makeFlagNavButton(true)  : null);
    }

    // =========================================================
    //  Filter rebuilding
    // =========================================================

    private void rebuildFilter(PaginatorSession session) {
        if (session.activeFlags.isEmpty()) {
            session.filteredTemplates = new ArrayList<>(session.baseTemplates);
        } else {
            session.filteredTemplates = session.baseTemplates.stream()
                    .filter(t -> session.activeFlags.stream().allMatch(f -> t.getAttributes().contains(f)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        session.totalPages = Math.max(1, (int) Math.ceil(session.filteredTemplates.size() / (double) ITEMS_PER_PAGE));
        session.page = Math.min(session.page, session.totalPages);
    }

    // =========================================================
    //  Event Handlers
    // =========================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var session = sessions.get(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;

        var topInv  = event.getView().getTopInventory();
        boolean inChest = event.getClickedInventory().equals(topInv);

        if (inChest) {
            handleChestClick(player, event.getRawSlot(), event.isLeftClick(), event.isRightClick(), topInv, session);
        } else {
            handlePlayerInvClick(player, event.getSlot(), topInv, session);
        }
    }

    private void handleChestClick(Player player, int slot, boolean leftClick, boolean rightClick,
                                   Inventory topInv, PaginatorSession session) {
        // Navigation row
        if (slot == SLOT_PREV && session.page > 1) {
            session.page--;
            fillChestContents(topInv, session);
            return;
        }
        if (slot == SLOT_NEXT && session.page < session.totalPages) {
            session.page++;
            fillChestContents(topInv, session);
            return;
        }
        if (slot == SLOT_PASTE_AIR) {
            session.pasteIgnoreAir = !session.pasteIgnoreAir;
            templateService.setPasteIgnoreAir(player.getUniqueId(), session.pasteIgnoreAir);
            fillChestContents(topInv, session);
            return;
        }
        if (slot == SLOT_RANDOM_ROTATION && session.mode == TemplatePaginatorMode.BRUSH) {
            session.randomRotation = !session.randomRotation;
            fillChestContents(topInv, session); // Regenerates with new random dice face!
            return;
        }
        if (slot >= 45) return; // filler / info — ignore

        // Template item click
        int index = (session.page - 1) * ITEMS_PER_PAGE + slot;
        if (index >= session.filteredTemplates.size()) return;
        var template = session.filteredTemplates.get(index);

        if (session.mode == TemplatePaginatorMode.SELECT) {
            if (leftClick) {
                player.closeInventory();
                player.performCommand("tcopy " + template.getTemplateName());
            } else if (rightClick) {
                // Give the player a template item instead of entering placement mode directly
                var templateItem = viewerService.createTemplateItem(template);
                player.getInventory().addItem(templateItem);
                player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("Template item received! ", NamedTextColor.GRAY))
                        .append(Component.text("Right-click", NamedTextColor.YELLOW))
                        .append(Component.text(" to enter placement mode.", NamedTextColor.GRAY)));
                player.closeInventory();
            }
        } else if (session.mode == TemplatePaginatorMode.BRUSH) {
            boolean isSelected = session.brush != null
                    && session.brush.getTemplates().contains(template.getTemplateName());
            // The brush command handler calls showBrushModifyGui → paginator.open() which handles
            // the full reopen. We only need to emit the command here.
            player.performCommand(isSelected
                    ? "tbrush remove template " + template.getTemplateName()
                    : "tbrush add template " + template.getTemplateName());
        }
    }

    private void handlePlayerInvClick(Player player, int slot, Inventory topInv, PaginatorSession session) {
        if (slot >= FLAG_SLOT_START && slot <= FLAG_SLOT_END) {
            // Toggle flag
            int flagIndex = session.flagPage * FLAGS_PER_PAGE + (slot - FLAG_SLOT_START);
            if (flagIndex >= session.allFlags.size()) return;
            String flag = session.allFlags.get(flagIndex);
            if (!session.activeFlags.remove(flag)) session.activeFlags.add(flag);
            rebuildFilter(session);
            populatePlayerInventory(player, session);
            fillChestContents(topInv, session);

        } else if (slot == HOTBAR_PREV && session.flagPage > 0) {
            session.flagPage--;
            populatePlayerInventory(player, session);

        } else if (slot == HOTBAR_NEXT && session.flagPage < session.totalFlagPages - 1) {
            session.flagPage++;
            populatePlayerInventory(player, session);

        } else if (slot == HOTBAR_CLEAR && !session.activeFlags.isEmpty()) {
            session.activeFlags.clear();
            rebuildFilter(session);
            populatePlayerInventory(player, session);
            fillChestContents(topInv, session);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        var session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        // Restore the player's pre-GUI inventory (hotbar + main rows)
        player.getInventory().setContents(session.cachedInventory);
    }

    // =========================================================
    //  Item builders — chest navigation
    // =========================================================

    private ItemStack makeFiller() {
        var item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePrevButton() {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("\u25C0 Previous Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeNextButton() {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Next Page \u25B6", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePasteAirToggle(boolean ignoreAir) {
        var item = new ItemStack(Material.FLOW_BANNER_PATTERN);
        var meta = item.getItemMeta();
        if (ignoreAir) {
            meta.displayName(Component.text("\u26A1 Paste: Ignore Air", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.setEnchantmentGlintOverride(true);
        } else {
            meta.displayName(Component.text("\u2601 Paste: Include Air", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        var lore = new ArrayList<Component>();
        lore.add(Component.text(ignoreAir
                ? "Air blocks in the schematic are skipped."
                : "Air blocks in the schematic overwrite existing blocks.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click to toggle", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get a random unicode dice face (⚀ ⚁ ⚂ ⚃ ⚄ ⚅)
     * @return A random dice character representing a roll from 1-6
     */
    private String getRandomDiceFace() {
        int roll = 1 + (int) (Math.random() * 6); // Roll 1-6
        return switch (roll) {
            case 1 -> "\u2680"; // ⚀
            case 2 -> "\u2681"; // ⚁
            case 3 -> "\u2682"; // ⚂
            case 4 -> "\u2683"; // ⚃
            case 5 -> "\u2684"; // ⚄
            case 6 -> "\u2685"; // ⚅
            default -> "\u2680";
        };
    }

    private ItemStack makeRandomRotation(boolean randomRotations) {
        var item = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        var meta = item.getItemMeta();
        
        // Roll the dice each time this is called
        String diceFace = getRandomDiceFace();
        
        if (randomRotations) {
            meta.displayName(Component.text(diceFace + " Random Rotations: ON", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.setEnchantmentGlintOverride(true);
        } else {
            meta.displayName(Component.text("25A1 Random Rotations: OFF", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        
        var lore = new ArrayList<Component>();
        lore.add(Component.text(randomRotations
                ? "Templates will be rotated randomly when placed."
                : "Templates will keep their original orientation.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click to toggle", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeInfoItem(PaginatorSession session) {
        var item = new ItemStack(Material.BOOK);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Page " + session.page + " / " + session.totalPages,
                NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        var lore = new ArrayList<Component>();
        lore.add(Component.text(session.filteredTemplates.size() + " templates", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        if (session.baseTemplates.size() != session.filteredTemplates.size()) {
            lore.add(Component.text("(" + session.baseTemplates.size() + " total)", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (session.filter != null && !session.filter.isBlank()) {
            lore.add(Component.text("Filter: " + session.filter, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (!session.activeFlags.isEmpty()) {
            lore.add(Component.text("Active flags: " + String.join(", ", session.activeFlags), NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Item builders — player inventory flags panel
    // =========================================================

    private ItemStack makeFlagItem(String flag, boolean active) {
        // Use the tag's custom display item if one is registered for this attribute
        var tag = templateService.getTag(flag);
        ItemStack item;
        if (tag != null && tag.hasDisplayItem()) {
            item = tag.getDisplayItem(); // already a clone
        } else {
            item = new ItemStack(active ? Material.LIME_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        }
        var meta = item.getItemMeta();
        meta.displayName(Component.text(flag, active ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        var lore = new ArrayList<Component>();
        if (active) {
            lore.add(Component.text("✔ Active filter", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click to remove", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Click to filter by this tag", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        }
        // Show if this tag has no custom icon configured
        if (tag == null) {
            lore.add(Component.text("⚠ No tag icon set — use /ttag create " + flag, NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, true));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeFlagNavButton(boolean next) {
        var item = new ItemStack(Material.SPECTRAL_ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(next ? "More Filters \u25B6" : "\u25C0 Prev Filters", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeClearFlagsButton() {
        var item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Clear All Filters", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeFlagInfoItem(PaginatorSession session) {
        var item = new ItemStack(Material.COMPASS);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Attribute Filters", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        var lore = new ArrayList<Component>();
        lore.add(Component.text("Page " + (session.flagPage + 1) + " / " + Math.max(1, session.totalFlagPages),
                NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        int active = session.activeFlags.size();
        lore.add(Component.text(active == 0 ? "No active filters" : active + " active",
                active == 0 ? NamedTextColor.DARK_GRAY : NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        if (!session.activeFlags.isEmpty()) {
            lore.add(Component.empty());
            session.activeFlags.forEach(f ->
                    lore.add(Component.text("  \u2022 " + f, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Template item builder
    // =========================================================

    private ItemStack createTemplateItem(Template template, PaginatorSession session) {
        ItemStack item = template.getDisplayItem();
        ItemMeta meta  = item.getItemMeta();

        boolean isSelected = session.brush != null
                && session.brush.getTemplates().contains(template.getTemplateName());

        var nameColor = isSelected ? NamedTextColor.AQUA : NamedTextColor.GREEN;
        meta.displayName(Component.text(template.getTemplateName(), nameColor, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        var lore = new ArrayList<Component>();
        lore.add(Component.text("Schematic: ", NamedTextColor.GRAY)
                .append(Component.text(template.getSchematicFile(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Dimensions: ", NamedTextColor.GRAY)
                .append(Component.text(template.getDimensions(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        long blockCount = template.getBlockCount();
        lore.add(Component.text("Blocks: ", NamedTextColor.GRAY)
                .append(Component.text(blockCount == -1 ? "Not loaded" : Long.toString(blockCount), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        if (!template.getAttributes().isEmpty()) {
            var joiner = new StringJoiner(", ");
            template.getAttributes().forEach(joiner::add);
            lore.add(Component.text("Attributes: ", NamedTextColor.GRAY)
                    .append(Component.text(joiner.toString(), NamedTextColor.GOLD))
                    .decoration(TextDecoration.ITALIC, false));
        }

        if (!template.hasDisplayItem()) {
            lore.add(Component.empty());
            lore.add(Component.text("\u26A0 No display item set", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, true));
        }

        lore.add(Component.empty());
        if (session.mode == TemplatePaginatorMode.BRUSH) {
            if (isSelected) {
                lore.add(Component.text("\u2714 Selected in brush", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Click to remove", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Click to add to brush", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("Left-click: Copy to clipboard", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Right-click: Get template item", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Title helper
    // =========================================================

    private String computeTitle(List<Template> sorted, int page) {
        if (sorted.isEmpty()) return "Templates (empty)";
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, sorted.size()) - 1;
        return "Templates (" + getSignificantPrefix(sorted.get(start).getTemplateName().toLowerCase(),
                sorted.get(end).getTemplateName().toLowerCase())
                + " - " + getSignificantPrefix(sorted.get(end).getTemplateName().toLowerCase(),
                sorted.get(start).getTemplateName().toLowerCase()) + ")";
    }

    private String getSignificantPrefix(String name, String other) {
        int common = 0, maxLen = Math.min(name.length(), other.length());
        while (common < maxLen && name.charAt(common) == other.charAt(common)) common++;
        return name.substring(0, Math.min(name.length(), Math.max(2, common + 1)));
    }

    // =========================================================
    //  Cleanup
    // =========================================================

    public void unregister() {
        HandlerList.unregisterAll(this);
        sessions.clear();
    }

    // =========================================================
    //  Enum
    // =========================================================

    public enum TemplatePaginatorMode {
        SELECT,
        BRUSH,
        CREATIVE
    }

    // =========================================================
    //  Session
    // =========================================================

    private static class PaginatorSession {
        // Template lists
        final List<Template> baseTemplates;
        List<Template> filteredTemplates;
        // Chest state
        final TemplatePaginatorMode mode;
        final TemplateBrush brush;
        final String filter;
        int page;
        int totalPages;
        // Flag (filter) state
        final List<String> allFlags;
        final Set<String> activeFlags;
        int flagPage;
        final int totalFlagPages;
        // Inventory preservation
        final ItemStack[] cachedInventory;
        // Callback (reserved)
        final BiConsumer<Player, Template> onSelect;
        // Paste preference
        boolean pasteIgnoreAir;
        // Random rotation preference
        boolean randomRotation;

        PaginatorSession(List<Template> baseTemplates, List<Template> filteredTemplates,
                         TemplatePaginatorMode mode, TemplateBrush brush, String filter,
                         int page, int totalPages,
                         List<String> allFlags, Set<String> activeFlags, int flagPage, int totalFlagPages,
                         ItemStack[] cachedInventory, BiConsumer<Player, Template> onSelect,
                         boolean pasteIgnoreAir, boolean randomRotation) {
            this.baseTemplates     = baseTemplates;
            this.filteredTemplates = filteredTemplates;
            this.mode              = mode;
            this.brush             = brush;
            this.filter            = filter;
            this.page              = page;
            this.totalPages        = totalPages;
            this.allFlags          = allFlags;
            this.activeFlags       = activeFlags;
            this.flagPage          = flagPage;
            this.totalFlagPages    = totalFlagPages;
            this.cachedInventory   = cachedInventory;
            this.onSelect          = onSelect;
            this.pasteIgnoreAir    = pasteIgnoreAir;
            this.randomRotation    = randomRotation;
        }
    }
}
