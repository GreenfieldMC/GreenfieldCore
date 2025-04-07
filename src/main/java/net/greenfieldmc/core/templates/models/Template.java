package net.greenfieldmc.core.templates.models;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import net.greenfieldmc.core.Triple;
import net.greenfieldmc.core.templates.paginators.TemplatePaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a template in the template system
 */
public class Template implements PageItem<Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> {

    private String templateName;
    private String schematicFile;
    private final List<String> attributes;
    private BlockArrayClipboard clipboard;

    public Template(String templateName) {
        this(templateName, null, List.of());
    }

    public Template(String templateName, String schematicFile, List<String> attributes) {
        this.templateName = templateName;
        this.schematicFile = schematicFile;
        this.attributes = attributes;
    }

    /**
     * Get the name of this template. This should be unique
     * @return the name of this template
     */
    public @NotNull String getTemplateName() {
        return templateName;
    }

    /**
     * Get the schematic file for this template
     * @return the schematic file for this template
     */
    public @NotNull String getSchematicFile() {
        return schematicFile;
    }

    /**
     * Get the attributes for this template
     * @return the attributes for this template
     */
    public @NotNull List<String> getAttributes() {
        return attributes;
    }

    /**
     * Set the attributes for this template
     * @param attributes the attributes to set
     */
    public void setAttributes(List<String> attributes) {
        this.attributes.clear();
        this.attributes.addAll(attributes);
    }

    /**
     * Check if this template has the given attribute
     * @param attribute the attribute to check
     * @return true if the template has the attribute, false otherwise
     */
    public boolean hasAttribute(String attribute) {
        return this.attributes.contains(attribute);
    }

    /**
     * Set the name of this template. This should be unique
     * @param templateName the name of the template to set
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Set the schematic file for this template. Note: this will not load the schematic file.
     * @param schematicFile the schematic file to set
     */
    public void setSchematicFile(String schematicFile)  {
        this.schematicFile = schematicFile;
        this.clipboard = null; // Reset the clipboard when the schematic file is changed
    }

    /**
     * Load the schematic file into a BlockArrayClipboard
     * @throws Exception If the file does not exist, or if the format is not supported, or if the schematic file could not be loaded.
     */
    public void loadClipboard() throws Exception {
        var file = new File(schematicFile);
        if (!file.exists()) throw new IOException("File '" + schematicFile + "' does not exist");
        var format = ClipboardFormats.findByFile(file);
        if (format == null) throw new Exception("No clipboard format found for file '" + schematicFile + "'");
        try (var reader = format.getReader(new FileInputStream(file))){
            this.clipboard = (BlockArrayClipboard) reader.read();
        }
    }

    /**
     * Get the loaded BlockArrayClipboard for this template
     * @return the clipboard for this template.
     */
    public @NotNull BlockArrayClipboard getClipboard() {
        if (clipboard == null) {
            throw new IllegalStateException("Clipboard is not loaded. Please load the clipboard before getting it.");
        }
        return clipboard;
    }

    /**
     * Check if the templates schematic is loaded
     * @return true if the template is loaded, false otherwise
     */
    public boolean isLoaded() {
        return clipboard != null;
    }

    /**
     * Get the dimensions of the template
     * @return the dimensions of the template
     */
    public String getDimensions() {
        if (!isLoaded()) return "Not yet loaded";
        
        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();
        
        int width = max.getBlockX() - min.getBlockX() + 1;
        int height = max.getBlockY() - min.getBlockY() + 1;
        int depth = max.getBlockZ() - min.getBlockZ() + 1;
        
        return width + "x" + height + "x" + depth;
    }
    
    /**
     * Get the block count of the template
     * @return the block count
     */
    public String getBlockCount() {
        if (!isLoaded()) return "Not yet loaded";
        return Integer.toString(clipboard.getRegion().getArea());
    }
    
    /**
     * Get the entity count of the template
     * @return the entity count
     */
    public String getEntityCount() {
        if (!isLoaded()) return "Not yet loaded";
        return Integer.toString(clipboard.getEntities().size());
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> paginator, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush> generatorInfo) {
        return getTemplateName();
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> paginator, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush> generatorInfo) {
        var mode = generatorInfo.getFirst();
        var context = generatorInfo.getSecond();
        var brush = generatorInfo.getThird();
        
        boolean isSelected = brush != null && brush.getTemplates().contains(getTemplateName());
        
        // Create the question mark with hover info
        TextComponent questionMark = createQuestionMark(paginator);
        
        var line = Component.text();
        line.append(questionMark).resetStyle().appendSpace();
        line.append(Component.text("[", NamedTextColor.GRAY, TextDecoration.BOLD));
        
        // Add buttons based on mode
        if (mode == TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY) {

            if (context.hasPermission("greenfieldcore.template.view")) {
                line.append(Component.text("V", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tview " + getTemplateName()))
                        .hoverEvent(HoverEvent.showText(Component.text("View this template", NamedTextColor.GRAY))));

                line.appendSpace();
            }

            if (context.hasPermission("greenfieldcore.template.copy")) {
                line.append(Component.text("C", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tcopy " + getTemplateName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Copy this template", NamedTextColor.GRAY))));

                line.appendSpace();
            }
            
            // Add/Remove template button
            if (isSelected) {
                // Template is in brush, show remove option
                line.append(Component.text("X", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tbrush remove template " + getTemplateName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Remove this template from brush", NamedTextColor.GRAY))));
            } else {
                // Template is not in brush, show add option
                line.append(Component.text("S", NamedTextColor.BLUE, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tbrush add template " + getTemplateName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Add this template to brush", NamedTextColor.GRAY))));
            }

        } else {

            // Just the view button in normal list mode
            line.append(Component.text("V", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/tview " + getTemplateName()))
                    .hoverEvent(HoverEvent.showText(Component.text("View this template", NamedTextColor.GRAY))));
        }

        line.append(Component.text("] - ", NamedTextColor.GRAY, TextDecoration.BOLD));

        var nameColor = isSelected ? paginator.getGrayedOutColor() : paginator.getHighlightColor();
        var nameFormatting = isSelected ? new TextDecoration[] { TextDecoration.UNDERLINED } : new TextDecoration[0];
        line.append(Component.text(getTemplateName(), nameColor, nameFormatting));
        
        return line.build();
    }
    
    private TextComponent createQuestionMark(ChatPaginator<?, ?> paginator) {
        var hoverText = Component.text();
        
        // Add block count
        hoverText.append(Component.text("Block Count: ", NamedTextColor.GRAY))
                .append(Component.text(getBlockCount(), NamedTextColor.BLUE));
        
        hoverText.appendNewline();
        
        // Add entity count
        hoverText.append(Component.text("Entity Count: ", NamedTextColor.GRAY))
                .append(Component.text(getEntityCount(), NamedTextColor.BLUE));
        
        hoverText.appendNewline();
        
        // Add dimensions
        hoverText.append(Component.text("Dimensions: ", NamedTextColor.GRAY))
                .append(Component.text(getDimensions(), NamedTextColor.BLUE));
        
        // Add attributes if there are any
        if (!attributes.isEmpty()) {
            hoverText.appendNewline();
            
            StringJoiner joiner = new StringJoiner(", ");
            for (String attribute : attributes) {
                joiner.add(attribute);
            }
            
            hoverText.append(Component.text("Attributes: ", NamedTextColor.GRAY))
                    .append(Component.text(joiner.toString(), NamedTextColor.BLUE));
        }
        
        return Component.text("?", paginator.getHighlightColor(), TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(hoverText.build()));
    }
}
