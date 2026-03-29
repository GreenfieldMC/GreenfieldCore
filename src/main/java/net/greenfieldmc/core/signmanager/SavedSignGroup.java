package net.greenfieldmc.core.signmanager;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a saved group of signs. A group appears as a single entry in the chat paginator
 * using the display sign (from the main hand at save time). Clicking the group entry
 * gives the player all member signs.
 */
public class SavedSignGroup {

    private final String name;
    private final SavedSign displaySign;
    private final List<SavedSign> memberSigns;

    public SavedSignGroup(String name, SavedSign displaySign, List<SavedSign> memberSigns) {
        this.name = name;
        this.displaySign = displaySign;
        this.memberSigns = new ArrayList<>(memberSigns);
    }

    public String getName() {
        return name;
    }

    public SavedSign getDisplaySign() {
        return displaySign;
    }

    public List<SavedSign> getMemberSigns() {
        return memberSigns;
    }

    /**
     * Returns all member signs as clean ItemStacks ready to be given to a player.
     */
    public List<ItemStack> toMemberItemStacks() {
        var items = new ArrayList<ItemStack>();
        for (var sign : memberSigns) {
            items.add(sign.toItemStack());
        }
        return items;
    }

    /**
     * Returns the combined plain text of the group for search matching.
     */
    public String getPlainText() {
        var sb = new StringBuilder();
        sb.append(name).append(" ");
        sb.append(displaySign.getPlainText()).append(" ");
        for (var sign : memberSigns) {
            sb.append(sign.getPlainText()).append(" ");
        }
        return sb.toString();
    }
}
