package net.greenfieldmc.core.templates.paginators.gui.builder;

import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.paginators.gui.session.TemplatePaginatorMode;
import net.greenfieldmc.core.templates.paginators.gui.session.TemplatePaginatorSession;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Factory class for creating all ItemStacks used in the template paginator GUI.
 * Handles creation of navigation buttons, toggles, info items, tag items, and template items.
 */
public class TemplateItemFactory {

    private final ITemplateService templateService;

    public TemplateItemFactory(ITemplateService templateService) {
        this.templateService = templateService;
    }

    // =========================================================
    //  Chest navigation items
    // =========================================================

    public ItemStack makeFiller() {
        var item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makePrevButton() {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("\u25C0 Previous Page", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeNextButton() {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Next Page \u25B6", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makePasteAirToggle(boolean ignoreAir) {
        var item = new ItemStack(Material.FLOW_BANNER_PATTERN);
        var meta = item.getItemMeta();
        
        if (ignoreAir) {
            meta.displayName(Component.text("\u26A1 Paste: Ignore Air", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            meta.setEnchantmentGlintOverride(true);
        } else {
            meta.displayName(Component.text("\u2601 Paste: Include Air", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        var lore = new ArrayList<Component>();
        lore.add(Component.text(ignoreAir
                        ? "Air blocks in the schematic are skipped."
                        : "Air blocks in the schematic overwrite existing blocks.",
                NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeRandomRotation(boolean randomRotations) {
        var item = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        var meta = item.getItemMeta();
        
        // Roll the dice each time this is called for visual variety
        String diceFace = getRandomDiceFace();
        
        if (randomRotations) {
            meta.displayName(Component.text(diceFace + " Random Rotations: ON", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            meta.setEnchantmentGlintOverride(true);
        } else {
            meta.displayName(Component.text("\u25A1 Random Rotations: OFF", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        var lore = new ArrayList<Component>();
        lore.add(Component.text(randomRotations
                        ? "Templates will be rotated randomly when placed."
                        : "Templates will keep their original orientation.",
                NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeInfoItem(TemplatePaginatorSession session) {
        var item = new ItemStack(Material.BOOK);
        var meta = item.getItemMeta();
        
        meta.displayName(Component.text("Page " + session.getPage() + " / " + session.getTotalPages(),
                        NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        var lore = new ArrayList<Component>();
        lore.add(Component.text(session.getFilteredTemplates().size() + " templates", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        if (session.getBaseTemplates().size() != session.getFilteredTemplates().size()) {
            lore.add(Component.text("(" + session.getBaseTemplates().size() + " total)", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        if (session.getFilter() != null && !session.getFilter().isBlank()) {
            lore.add(Component.text("Filter: " + session.getFilter(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        if (!session.getActiveTags().isEmpty()) {
            lore.add(Component.text("Active tags: " + String.join(", ", session.getActiveTags()),
                            NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Player inventory tag items
    // =========================================================

    public ItemStack makeTagItem(String tag, boolean active) {
        // Use the tag's custom display item if one is registered for this attribute
        var tagObj = templateService.getTag(tag);
        ItemStack item;
        
        if (tagObj != null && tagObj.hasDisplayItem()) {
            item = tagObj.getDisplayItem(); // already a clone
        } else {
            item = new ItemStack(active ? Material.LIME_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        }
        
        var meta = item.getItemMeta();
        meta.displayName(Component.text(tag, active ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        var lore = new ArrayList<Component>();
        if (active) {
            lore.add(Component.text("✔ Active filter", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click to remove", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Click to filter by this tag", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
        
        // Show warning if this tag has no custom icon configured
        if (tagObj == null || !tagObj.hasDisplayItem()) {
            lore.add(Component.text("⚠ No tag icon set — use /ttag create " + tag,
                            NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, true));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeTagNavButton(boolean next) {
        var item = new ItemStack(Material.SPECTRAL_ARROW);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(next ? "More Filters \u25B6" : "\u25C0 Prev Filters",
                        NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeClearTagsButton() {
        var item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Clear All Filters", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeTagInfoItem(TemplatePaginatorSession session) {
        var item = new ItemStack(Material.COMPASS);
        var meta = item.getItemMeta();
        
        meta.displayName(Component.text("Attribute Filters", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        
        var lore = new ArrayList<Component>();
        lore.add(Component.text("Page " + (session.getTagPage() + 1) + " / " +
                                Math.max(1, session.getTotalTagPages()),
                        NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        int active = session.getActiveTags().size();
        lore.add(Component.text(active == 0 ? "No active filters" : active + " active",
                        active == 0 ? NamedTextColor.DARK_GRAY : NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        if (!session.getActiveTags().isEmpty()) {
            lore.add(Component.empty());
            session.getActiveTags().forEach(f ->
                    lore.add(Component.text("  \u2022 " + f, NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false)));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Template items
    // =========================================================

    public ItemStack createTemplateItem(Template template, TemplatePaginatorSession session) {
        ItemStack item = template.getDisplayItem();
        ItemMeta meta = item.getItemMeta();

        boolean isSelected = session.getBrush() != null
                && session.getBrush().getTemplates().contains(template.getTemplateName());

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
                .append(Component.text(blockCount == -1 ? "Not loaded" : Long.toString(blockCount),
                        NamedTextColor.WHITE))
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
        
        if (session.getMode() == TemplatePaginatorMode.BRUSH) {
            if (isSelected) {
                lore.add(Component.text("\u2714 Selected in brush", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Click to remove", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Click to add to brush", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("Left-click: Copy to clipboard", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Right-click: Get template item", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Helpers
    // =========================================================

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
}

