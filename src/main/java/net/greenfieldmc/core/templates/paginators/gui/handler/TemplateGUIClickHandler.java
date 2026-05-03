package net.greenfieldmc.core.templates.paginators.gui.handler;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.paginators.gui.builder.TemplateInventoryBuilder;
import net.greenfieldmc.core.templates.models.TemplatePaginatorMode;
import net.greenfieldmc.core.templates.paginators.gui.session.TemplatePaginatorSession;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static net.greenfieldmc.core.templates.paginators.gui.TemplatePaginatorConstants.*;

/**
 * Handles click events for the template paginator GUI.
 * Processes both chest inventory clicks and player inventory clicks.
 */
public class TemplateGUIClickHandler {

    private final ITemplateService templateService;
    private final ITemplateViewerService viewerService;
    private final TemplateInventoryBuilder inventoryBuilder;

    public TemplateGUIClickHandler(ITemplateService templateService,
                                   ITemplateViewerService viewerService,
                                   TemplateInventoryBuilder inventoryBuilder) {
        this.templateService = templateService;
        this.viewerService = viewerService;
        this.inventoryBuilder = inventoryBuilder;
    }

    // =========================================================
    //  Chest click handling
    // =========================================================

    /**
     * Handle a click in the chest inventory (top inventory).
     *
     * @param player the player who clicked
     * @param slot the raw slot that was clicked
     * @param leftClick whether it was a left click
     * @param rightClick whether it was a right click
     * @param topInv the top inventory (chest)
     * @param session the paginator session
     */
    public void handleChestClick(Player player, int slot, boolean leftClick, boolean rightClick,
                                 Inventory topInv, TemplatePaginatorSession session) {
        
        // ---- Navigation row ----
        if (slot == SLOT_PREV && session.prevPage()) {
            inventoryBuilder.fillChestContents(topInv, session);
            return;
        }
        
        if (slot == SLOT_NEXT && session.nextPage()) {
            inventoryBuilder.fillChestContents(topInv, session);
            return;
        }
        
        if (slot == SLOT_PASTE_AIR) {
            session.setPasteIgnoreAir(!session.isPasteIgnoreAir());
            templateService.setPasteIgnoreAir(player.getUniqueId(), session.isPasteIgnoreAir());
            inventoryBuilder.fillChestContents(topInv, session);
            return;
        }
        
        if (slot == SLOT_RANDOM_ROTATION && session.getMode() == TemplatePaginatorMode.BRUSH) {
            session.setRandomRotation(!session.isRandomRotation());
            inventoryBuilder.fillChestContents(topInv, session); // Regenerates with new random dice face!
            return;
        }
        
        // Ignore clicks on filler/info items
        if (slot >= 45) {
            return;
        }

        // ---- Template item click ----
        int index = (session.getPage() - 1) * ITEMS_PER_PAGE + slot;
        if (index >= session.getFilteredTemplates().size()) {
            return;
        }
        
        var template = session.getFilteredTemplates().get(index);
        handleTemplateClick(player, template, leftClick, rightClick, session);
    }

    /**
     * Handle a click on a specific template item.
     */
    private void handleTemplateClick(Player player, Template template, boolean leftClick,
                                     boolean rightClick, TemplatePaginatorSession session) {
        
        if (session.getMode() == TemplatePaginatorMode.SELECT) {
            if (leftClick) {
                // Copy template to WorldEdit clipboard
                player.closeInventory();
                player.performCommand("tcopy " + template.getTemplateName());
                
            } else if (rightClick) {
                // Give the player a template item with encoded placement options
                var templateItem = viewerService.createTemplateItem(
                        template,
                        session.isPasteIgnoreAir(),
                        session.isPasteIncludeEntities()
                );
                
                player.getInventory().addItem(templateItem);
                player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("Template item received! ", NamedTextColor.GRAY))
                        .append(Component.text("Right-click", NamedTextColor.YELLOW))
                        .append(Component.text(" to enter placement mode.", NamedTextColor.GRAY)));
                player.closeInventory();
            }
            
        } else if (session.getMode() == TemplatePaginatorMode.BRUSH) {
            boolean isSelected = session.getBrush() != null
                    && session.getBrush().getTemplates().contains(template.getTemplateName());

            // The brush command handler calls showBrushModifyGui → paginator.open() which handles
            // the full reopen. We only need to emit the command here.
            player.performCommand(isSelected
                    ? "tbrush remove template " + template.getTemplateName()
                    : "tbrush add template " + template.getTemplateName());
        }
    }

    // =========================================================
    //  Player inventory click handling
    // =========================================================

    /**
     * Handle a click in the player's inventory (bottom inventory).
     *
     * @param player the player who clicked
     * @param slot the slot that was clicked (0-based player inventory slot)
     * @param topInv the top inventory (chest)
     * @param session the paginator session
     */
    public void handlePlayerInvClick(Player player, int slot, Inventory topInv,
                                     TemplatePaginatorSession session) {
        
        // ---- Tag filter item click ----
        if (slot >= TAG_SLOT_START && slot <= TAG_SLOT_END) {
            int tagIndex = session.getTagPage() * TAGS_PER_PAGE + (slot - TAG_SLOT_START);
            if (tagIndex >= session.getAllTags().size()) {
                return;
            }
            
            String tag = session.getAllTags().get(tagIndex);
            session.toggleTag(tag);
            session.rebuildFilter();
            
            inventoryBuilder.populatePlayerInventory(player, session);
            inventoryBuilder.fillChestContents(topInv, session);
            return;
        }

        // ---- Tag navigation hotbar ----
        if (slot == HOTBAR_PREV && session.prevTagPage()) {
            inventoryBuilder.populatePlayerInventory(player, session);
            return;
        }
        
        if (slot == HOTBAR_NEXT && session.nextTagPage()) {
            inventoryBuilder.populatePlayerInventory(player, session);
            return;
        }
        
        if (slot == HOTBAR_CLEAR && !session.getActiveTags().isEmpty()) {
            session.clearTags();
            session.rebuildFilter();
            inventoryBuilder.populatePlayerInventory(player, session);
            inventoryBuilder.fillChestContents(topInv, session);
        }
    }
}

