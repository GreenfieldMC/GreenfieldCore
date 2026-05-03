package net.greenfieldmc.core.templates.paginators.gui;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.paginators.gui.builder.TemplateInventoryBuilder;
import net.greenfieldmc.core.templates.paginators.gui.builder.TemplateItemFactory;
import net.greenfieldmc.core.templates.paginators.gui.handler.TemplateGUIClickHandler;
import net.greenfieldmc.core.templates.models.TemplatePaginatorMode;
import net.greenfieldmc.core.templates.paginators.gui.session.TemplatePaginatorSession;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static net.greenfieldmc.core.templates.paginators.gui.TemplatePaginatorConstants.*;

/**
 * A double-chest GUI paginator for browsing templates.
 * Refactored for better code organization and maintainability.
 *
 * <p>Layout:
 * <ul>
 *   <li><b>Top inventory (chest, 54 slots)</b> — template items (rows 1-5) + navigation row (row 6)</li>
 *   <li><b>Player main inventory (slots 9-35)</b> — 27 attribute-tag filter items (3 rows)</li>
 *   <li><b>Player hotbar (slots 0-8)</b> — tag navigation: prev(0), clear(1), info(4), next(8)</li>
 * </ul>
 *
 * <p>The player's inventory is cached on open and fully restored on close.
 */
public class TemplateGUIPaginatorService implements Listener {

    private final Plugin plugin;
    private final ITemplateService templateService;
    private final ITemplateViewerService viewerService;
    private final Map<UUID, TemplatePaginatorSession> sessions = new HashMap<>();

    // Delegate components
    private final TemplateItemFactory itemFactory;
    private final TemplateInventoryBuilder inventoryBuilder;
    private final TemplateGUIClickHandler clickHandler;

    public TemplateGUIPaginatorService(Plugin plugin, ITemplateService templateService,
                                      ITemplateViewerService viewerService) {
        this.plugin = plugin;
        this.templateService = templateService;
        this.viewerService = viewerService;

        // Initialize components
        this.itemFactory = new TemplateItemFactory(templateService);
        this.inventoryBuilder = new TemplateInventoryBuilder(itemFactory);
        this.clickHandler = new TemplateGUIClickHandler(templateService, viewerService, inventoryBuilder);

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

        // Collect all unique attributes that exist in the base list → become tag items
        var allTags = base.stream()
                .flatMap(t -> t.getAttributes().stream())
                .distinct().sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        int totalTagPages = Math.max(1, (int) Math.ceil(allTags.size() / (double) TAGS_PER_PAGE));
        var filtered = new ArrayList<>(base);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        // If a session already exists for this player (e.g. from a brush-command reopen),
        // preserve the original inventory cache so we restore the real items on close.
        var existingSession = sessions.get(player.getUniqueId());
        var cachedInv = (existingSession != null)
                ? existingSession.getCachedInventory()
                : player.getInventory().getContents().clone();

        // Preserve active tags across brush reopens
        Set<String> activeTags = (existingSession != null)
                ? existingSession.getActiveTags()
                : new LinkedHashSet<>();
        int tagPage = (existingSession != null) ? existingSession.getTagPage() : 0;
        tagPage = Math.min(tagPage, totalTagPages - 1);

        // Read paste-ignore-air preference from the persistent player session (default true)
        boolean pasteIgnoreAir = templateService.isPasteIgnoreAir(player.getUniqueId());

        // Preserve random rotation state across reopens (default false)
        boolean randomRotation = (existingSession != null) && existingSession.isRandomRotation();

        var session = new TemplatePaginatorSession(base, filtered, mode, brush, initialFilter,
                page, totalPages, allTags, activeTags, tagPage, totalTagPages,
                cachedInv, onSelect, pasteIgnoreAir, randomRotation);

        // Re-apply any active tags to the new base list
        if (!activeTags.isEmpty()) {
            session.rebuildFilter();
        }

        sessions.put(player.getUniqueId(), session);

        // Populate tag area in player inventory BEFORE opening the chest
        inventoryBuilder.populatePlayerInventory(player, session);
        player.openInventory(inventoryBuilder.buildChestInventory(session));
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

        var topInv = event.getView().getTopInventory();
        boolean inChest = event.getClickedInventory().equals(topInv);

        if (inChest) {
            clickHandler.handleChestClick(player, event.getRawSlot(), event.isLeftClick(),
                    event.isRightClick(), topInv, session);
        } else {
            clickHandler.handlePlayerInvClick(player, event.getSlot(), topInv, session);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        var session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        // Restore the player's pre-GUI inventory (hotbar + main rows)
        player.getInventory().setContents(session.getCachedInventory());
    }

    // =========================================================
    //  Cleanup
    // =========================================================

    public void unregister() {
        HandlerList.unregisterAll(this);
        sessions.clear();
    }
}
