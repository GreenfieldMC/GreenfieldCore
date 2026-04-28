package net.greenfieldmc.core.templates.models;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Represents a template in the template system
 */
public class Template {

    private String templateName;
    private String schematicFile;
    private final List<String> attributes;
    private BlockArrayClipboard clipboard;
    private ItemStack displayItem;

    public Template(String templateName) {
        this(templateName, null, List.of(), null);
    }

    public Template(String templateName, String schematicFile, List<String> attributes) {
        this(templateName, schematicFile, attributes, null);
    }

    public Template(String templateName, String schematicFile, List<String> attributes, @Nullable ItemStack displayItem) {
        this.templateName = templateName;
        this.schematicFile = schematicFile;
        this.attributes = new java.util.ArrayList<>(attributes);
        this.displayItem = displayItem;
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
     * Get the display item for this template. Used to represent the template in the GUI.
     * @return the display item, or a default PAPER item if none is set.
     */
    public @NotNull ItemStack getDisplayItem() {
        if (displayItem == null) {
            return new ItemStack(Material.PAPER);
        }
        return displayItem.clone();
    }

    /**
     * Set the display item for this template. This item represents the template in the GUI.
     * @param displayItem the display item to set
     */
    public void setDisplayItem(@Nullable ItemStack displayItem) {
        this.displayItem = displayItem != null ? displayItem.clone() : null;
    }

    /**
     * Check if this template has a custom display item set
     * @return true if a display item has been set, false otherwise
     */
    public boolean hasDisplayItem() {
        return displayItem != null;
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
        
        int width = max.x() - min.x() + 1;
        int height = max.y() - min.y() + 1;
        int depth = max.z() - min.z() + 1;
        
        return width + "x" + height + "x" + depth;
    }
    
    /**
     * Get the block count of the template
     * @return the block count
     */
    public long getBlockCount() {
        if (!isLoaded()) return -1;
        return clipboard.getRegion().getVolume();
    }
    
    /**
     * Get the entity count of the template
     * @return the entity count
     */
    public String getEntityCount() {
        if (!isLoaded()) return "Not yet loaded";
        return Integer.toString(clipboard.getEntities().size());
    }
}
