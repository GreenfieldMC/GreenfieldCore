package net.greenfieldmc.core.templates.paginators.gui.builder;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplatePaginatorMode;
import net.greenfieldmc.core.templates.paginators.gui.session.TemplatePaginatorSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

import static net.greenfieldmc.core.templates.paginators.gui.TemplatePaginatorConstants.*;

/**
 * Builds and updates inventory views for the template paginator GUI.
 * Handles both the chest inventory and player inventory modifications.
 */
public class TemplateInventoryBuilder {

    private final TemplateItemFactory itemFactory;

    public TemplateInventoryBuilder(TemplateItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    // =========================================================
    //  Chest inventory
    // =========================================================

    /**
     * Build a new chest inventory with the given session data.
     * @param session the paginator session
     * @return a new Inventory ready to be opened
     */
    public Inventory buildChestInventory(TemplatePaginatorSession session) {
        var inv = Bukkit.createInventory(null, CHEST_SIZE,
                Component.text(computeTitle(session.getFilteredTemplates(), session.getPage()),
                        NamedTextColor.DARK_PURPLE));
        fillChestContents(inv, session);
        return inv;
    }

    /**
     * Update the chest inventory in-place. Avoids firing InventoryCloseEvent during navigation.
     * @param inv the inventory to update
     * @param session the paginator session
     */
    public void fillChestContents(Inventory inv, TemplatePaginatorSession session) {
        inv.clear();
        
        var sorted = session.getFilteredTemplates();
        int start = (session.getPage() - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, sorted.size());
        
        // Template items (rows 1-5)
        for (int i = start; i < end; i++) {
            inv.setItem(i - start, itemFactory.createTemplateItem(sorted.get(i), session));
        }

        // Navigation row (row 6)
        var filler = itemFactory.makeFiller();
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, filler);
        }
        
        inv.setItem(SLOT_PREV, session.getPage() > 1 ? itemFactory.makePrevButton() : filler);
        inv.setItem(SLOT_PASTE_AIR, itemFactory.makePasteAirToggle(session.isPasteIgnoreAir()));
        inv.setItem(SLOT_INFO, itemFactory.makeInfoItem(session));
        inv.setItem(SLOT_RANDOM_ROTATION, 
                session.getMode() == TemplatePaginatorMode.BRUSH
                        ? itemFactory.makeRandomRotation(session.isRandomRotation()) 
                        : filler);
        inv.setItem(SLOT_NEXT, session.getPage() < session.getTotalPages() ? itemFactory.makeNextButton() : filler);
    }

    // =========================================================
    //  Player inventory (tags panel)
    // =========================================================

    /**
     * Populate the player's inventory with tag filter items and navigation.
     * @param player the player
     * @param session the paginator session
     */
    public void populatePlayerInventory(Player player, TemplatePaginatorSession session) {
        var inv = player.getInventory();

        // Clear the 3rd inventory row (slots 18-26)
        for (int i = 18; i < 27; i++) {
            inv.setItem(i, null);
        }
        
        // Clear tag slots
        for (int i = TAG_SLOT_START; i <= TAG_SLOT_END; i++) {
            inv.setItem(i, null);
        }

        // Tag items (main inventory rows 1-3, player slots 9-26)
        var allTags = session.getAllTags();
        int tagStart = session.getTagPage() * TAGS_PER_PAGE;
        int tagEnd = Math.min(tagStart + TAGS_PER_PAGE, allTags.size());
        
        for (int i = tagStart; i < tagEnd; i++) {
            String tag = allTags.get(i);
            inv.setItem(TAG_SLOT_START + (i - tagStart),
                    itemFactory.makeTagItem(tag, session.getActiveTags().contains(tag)));
        }

        // Hotbar navigation
        inv.setItem(HOTBAR_PREV, session.getTagPage() > 0 ? itemFactory.makeTagNavButton(false) : null);
        inv.setItem(HOTBAR_CLEAR, !session.getActiveTags().isEmpty() ? itemFactory.makeClearTagsButton() : null);
        inv.setItem(HOTBAR_INFO, itemFactory.makeTagInfoItem(session));
        inv.setItem(HOTBAR_NEXT, session.getTagPage() < session.getTotalTagPages() - 1 
                ? itemFactory.makeTagNavButton(true) : null);
    }

    // =========================================================
    //  Title helper
    // =========================================================

    /**
     * Compute a dynamic title showing the alphabetical range of templates on the current page.
     * @param sorted the sorted template list
     * @param page the current page (1-based)
     * @return a title string
     */
    private String computeTitle(List<Template> sorted, int page) {
        if (sorted.isEmpty()) {
            return "Templates (empty)";
        }
        
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, sorted.size()) - 1;
        
        String firstPrefix = getSignificantPrefix(
                sorted.get(start).getTemplateName().toLowerCase(),
                sorted.get(end).getTemplateName().toLowerCase());
        String lastPrefix = getSignificantPrefix(
                sorted.get(end).getTemplateName().toLowerCase(),
                sorted.get(start).getTemplateName().toLowerCase());
        
        return "Templates (" + firstPrefix + " - " + lastPrefix + ")";
    }

    /**
     * Get a significant prefix from a name, considering what differs from another name.
     * Used to create compact, meaningful page titles.
     *
     * @param name the name to get a prefix from
     * @param other the other name to compare against
     * @return a prefix string (at least 2 characters)
     */
    private String getSignificantPrefix(String name, String other) {
        int common = 0;
        int maxLen = Math.min(name.length(), other.length());
        
        while (common < maxLen && name.charAt(common) == other.charAt(common)) {
            common++;
        }
        
        return name.substring(0, Math.min(name.length(), Math.max(2, common + 1)));
    }
}

