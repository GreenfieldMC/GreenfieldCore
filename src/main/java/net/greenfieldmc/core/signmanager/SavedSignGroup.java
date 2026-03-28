package net.greenfieldmc.core.signmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a saved group of signs. A group appears as a single entry in the GUI
 * using the display sign (from the main hand at save time). Clicking the group in the
 * GUI gives the player all member signs.
 */
public class SavedSignGroup {

    private final String name;
    private final SavedSign displaySign;
    private final List<SavedSign> memberSigns;
    private @Nullable SignFlag flag;

    public SavedSignGroup(String name, SavedSign displaySign, List<SavedSign> memberSigns, @Nullable SignFlag flag) {
        this.name = name;
        this.displaySign = displaySign;
        this.memberSigns = new ArrayList<>(memberSigns);
        this.flag = flag;
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

    public @Nullable SignFlag getFlag() {
        return flag;
    }

    public void setFlag(@Nullable SignFlag flag) {
        this.flag = flag;
    }

    /**
     * Creates the display ItemStack for the GUI.
     * Uses the display sign's material and text, plus group metadata in the lore.
     */
    public ItemStack toDisplayItemStack() {
        var item = displaySign.toItemStack();
        var meta = item.getItemMeta();

        // Override the display name to show the group name
        meta.displayName(Component.text(name, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

        // Rebuild lore: display sign text + group info
        var lore = new ArrayList<Component>();

        // Show the display sign's text in lore
        lore.addAll(displaySign.buildLoreLines());

        lore.add(Component.empty());
        lore.add(Component.text("⬐ Group: ", NamedTextColor.DARK_GRAY)
                .append(Component.text(name, NamedTextColor.YELLOW))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Contains " + memberSigns.size() + " sign(s)", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        if (flag != null) {
            lore.add(Component.text("Flag: ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(flag.getDisplayName(), NamedTextColor.AQUA))
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns all member signs as clean ItemStacks (no display name / lore metadata)
     * ready to be given to a player.
     */
    public List<ItemStack> toMemberItemStacks() {
        var items = new ArrayList<ItemStack>();
        for (var sign : memberSigns) {
            var signItem = sign.toItemStack();
            var meta = signItem.getItemMeta();
            meta.displayName(null);
            meta.lore(null);
            signItem.setItemMeta(meta);
            items.add(signItem);
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
        if (flag != null) sb.append(flag.getDisplayName());
        return sb.toString();
    }
}

