package net.greenfieldmc.core.signmanager;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a single entry in the Sign Manager GUI.
 * Can be either an individual SavedSign or a SavedSignGroup.
 * Groups appear as a single item using the display sign, but give all member signs when clicked.
 */
@SuppressWarnings("DataFlowIssue")
public class SignManagerEntry {

    private final @Nullable SavedSign sign;
    private final @Nullable SavedSignGroup group;

    private SignManagerEntry(@Nullable SavedSign sign, @Nullable SavedSignGroup group) {
        this.sign = sign;
        this.group = group;
    }

    public static SignManagerEntry ofSign(SavedSign sign) {
        return new SignManagerEntry(sign, null);
    }

    public static SignManagerEntry ofGroup(SavedSignGroup group) {
        return new SignManagerEntry(null, group);
    }

    public boolean isGroup() {
        return group != null;
    }

    public String getName() {
        return isGroup() ? group.getName() : sign.getName();
    }

    public @Nullable SignFlag getFlag() {
        return isGroup() ? group.getFlag() : sign.getFlag();
    }

    /**
     * Returns the display item for the GUI slot.
     */
    public ItemStack toDisplayItemStack() {
        return isGroup() ? group.toDisplayItemStack() : sign.toItemStack();
    }

    /**
     * Returns all sign items to give the player when this entry is clicked.
     * For a single sign, returns one clean item. For a group, returns all member items.
     */
    public List<ItemStack> toGiveItemStacks() {
        if (isGroup()) {
            return group.toMemberItemStacks();
        } else {
            var signItem = sign.toItemStack();
            var meta = signItem.getItemMeta();
            meta.displayName(null);
            meta.lore(null);
            signItem.setItemMeta(meta);
            return List.of(signItem);
        }
    }

    /**
     * Returns the plain text content for search matching.
     */
    public String getPlainText() {
        return isGroup() ? group.getPlainText() : sign.getPlainText();
    }
}

