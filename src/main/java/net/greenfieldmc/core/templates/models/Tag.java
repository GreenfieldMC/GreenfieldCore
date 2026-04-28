package net.greenfieldmc.core.templates.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a named tag that can be applied to templates as an attribute.
 * Tags carry a custom display ItemStack used to represent them in the GUI filter panel.
 */
public class Tag {

    private final String name;
    private ItemStack displayItem;

    public Tag(String name, @Nullable ItemStack displayItem) {
        this.name = name;
        this.displayItem = displayItem != null ? displayItem.clone() : null;
    }

    /** The tag name — this should match the attribute string used on {@link Template}s. */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Get the display item used to represent this tag in the GUI.
     * Returns a default KNOWLEDGE_BOOK item if none has been set.
     */
    public @NotNull ItemStack getDisplayItem() {
        return displayItem != null ? displayItem.clone() : new ItemStack(Material.KNOWLEDGE_BOOK);
    }

    public void setDisplayItem(@Nullable ItemStack displayItem) {
        this.displayItem = displayItem != null ? displayItem.clone() : null;
    }

    public boolean hasDisplayItem() {
        return displayItem != null;
    }
}

