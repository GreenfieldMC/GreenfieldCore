package net.greenfieldmc.core.templates.paginators.gui.session;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplatePaginatorMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static net.greenfieldmc.core.templates.paginators.gui.TemplatePaginatorConstants.ITEMS_PER_PAGE;

/**
 * Session data for an active template paginator GUI.
 * Manages the state of filtering, pagination, and user preferences.
 */
public class TemplatePaginatorSession {
    
    // =========================================================
    //  Template lists
    // =========================================================
    
    private final List<Template> baseTemplates;
    private List<Template> filteredTemplates;
    
    // =========================================================
    //  Chest state
    // =========================================================
    
    private final TemplatePaginatorMode mode;
    private final TemplateBrush brush;
    private final String filter;
    private int page;
    private int totalPages;
    
    // =========================================================
    //  Tag (filter) state
    // =========================================================
    
    private final List<String> allTags;
    private final Set<String> activeTags;
    private int tagPage;
    private final int totalTagPages;
    
    // =========================================================
    //  Inventory preservation
    // =========================================================
    
    private final ItemStack[] cachedInventory;
    
    // =========================================================
    //  Callback (reserved)
    // =========================================================
    
    private final BiConsumer<Player, Template> onSelect;
    
    // =========================================================
    //  Paste preferences
    // =========================================================
    
    private boolean pasteIgnoreAir;
    private boolean pasteIncludeEntities;
    private boolean randomRotation;

    // =========================================================
    //  Constructor
    // =========================================================

    public TemplatePaginatorSession(List<Template> baseTemplates, List<Template> filteredTemplates,
                                    TemplatePaginatorMode mode, TemplateBrush brush, String filter,
                                    int page, int totalPages,
                                    List<String> allTags, Set<String> activeTags, int tagPage, int totalTagPages,
                                    ItemStack[] cachedInventory, BiConsumer<Player, Template> onSelect,
                                    boolean pasteIgnoreAir, boolean randomRotation) {
        this.baseTemplates = baseTemplates;
        this.filteredTemplates = filteredTemplates;
        this.mode = mode;
        this.brush = brush;
        this.filter = filter;
        this.page = page;
        this.totalPages = totalPages;
        this.allTags = allTags;
        this.activeTags = activeTags;
        this.tagPage = tagPage;
        this.totalTagPages = totalTagPages;
        this.cachedInventory = cachedInventory;
        this.onSelect = onSelect;
        this.pasteIgnoreAir = pasteIgnoreAir;
        this.randomRotation = randomRotation;
        this.pasteIncludeEntities = true; // Default to true
    }

    // =========================================================
    //  Filter management
    // =========================================================

    /**
     * Rebuild the filtered template list based on active tags.
     * Updates totalPages and adjusts current page if necessary.
     */
    public void rebuildFilter() {
        if (activeTags.isEmpty()) {
            filteredTemplates = new ArrayList<>(baseTemplates);
        } else {
            filteredTemplates = baseTemplates.stream()
                    .filter(t -> activeTags.stream().allMatch(f -> t.getAttributes().contains(f)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        totalPages = Math.max(1, (int) Math.ceil(filteredTemplates.size() / (double) ITEMS_PER_PAGE));
        page = Math.min(page, totalPages);
    }

    /**
     * Toggle a tag filter on/off.
     * @param tag the tag to toggle
     * @return true if the tag was added, false if it was removed
     */
    public boolean toggleTag(String tag) {
        if (!activeTags.remove(tag)) {
            activeTags.add(tag);
            return true;
        }
        return false;
    }

    /**
     * Clear all active tag filters.
     */
    public void clearTags() {
        activeTags.clear();
    }

    // =========================================================
    //  Page navigation
    // =========================================================

    /**
     * Move to the next page if possible.
     * @return true if the page was changed
     */
    public boolean nextPage() {
        if (page < totalPages) {
            page++;
            return true;
        }
        return false;
    }

    /**
     * Move to the previous page if possible.
     * @return true if the page was changed
     */
    public boolean prevPage() {
        if (page > 1) {
            page--;
            return true;
        }
        return false;
    }

    /**
     * Move to the next tag page if possible.
     * @return true if the page was changed
     */
    public boolean nextTagPage() {
        if (tagPage < totalTagPages - 1) {
            tagPage++;
            return true;
        }
        return false;
    }

    /**
     * Move to the previous tag page if possible.
     * @return true if the page was changed
     */
    public boolean prevTagPage() {
        if (tagPage > 0) {
            tagPage--;
            return true;
        }
        return false;
    }

    // =========================================================
    //  Getters
    // =========================================================

    public List<Template> getBaseTemplates() {
        return baseTemplates;
    }

    public List<Template> getFilteredTemplates() {
        return filteredTemplates;
    }

    public TemplatePaginatorMode getMode() {
        return mode;
    }

    @Nullable
    public TemplateBrush getBrush() {
        return brush;
    }

    @Nullable
    public String getFilter() {
        return filter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<String> getAllTags() {
        return allTags;
    }

    public Set<String> getActiveTags() {
        return activeTags;
    }

    public int getTagPage() {
        return tagPage;
    }

    public int getTotalTagPages() {
        return totalTagPages;
    }

    public ItemStack[] getCachedInventory() {
        return cachedInventory;
    }

    @Nullable
    public BiConsumer<Player, Template> getOnSelect() {
        return onSelect;
    }

    public boolean isPasteIgnoreAir() {
        return pasteIgnoreAir;
    }

    public void setPasteIgnoreAir(boolean pasteIgnoreAir) {
        this.pasteIgnoreAir = pasteIgnoreAir;
    }

    public boolean isPasteIncludeEntities() {
        return pasteIncludeEntities;
    }

    public void setPasteIncludeEntities(boolean pasteIncludeEntities) {
        this.pasteIncludeEntities = pasteIncludeEntities;
    }

    public boolean isRandomRotation() {
        return randomRotation;
    }

    public void setRandomRotation(boolean randomRotation) {
        this.randomRotation = randomRotation;
    }
}

