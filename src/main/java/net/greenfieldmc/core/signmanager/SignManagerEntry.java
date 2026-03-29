package net.greenfieldmc.core.signmanager;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a single entry in the Sign Manager paginator.
 * Can be either an individual SavedSign or a SavedSignGroup.
 * Groups appear as a single line using the display sign info, but give all member signs when clicked.
 */
@SuppressWarnings("DataFlowIssue")
public class SignManagerEntry implements PageItem<ICommandContext> {

    // Unicode block characters for sign type indicators
    // Full block for normal signs, lower half block for hanging signs
    private static final String BLOCK_FULL = "█";       // U+2588 Full Block
    private static final String BLOCK_LOWER_HALF = "▄";  // U+2584 Lower Half Block
    private static final String BLOCK_THREE_QUARTER = "▆"; // U+2586 Lower Three Quarters Block

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

    /**
     * Returns the display sign — either the individual sign itself or the group's display sign.
     */
    public SavedSign getDisplaySign() {
        return isGroup() ? group.getDisplaySign() : sign;
    }

    /**
     * Returns all sign items to give the player when this entry is clicked.
     */
    public List<ItemStack> toGiveItemStacks() {
        if (isGroup()) {
            return group.toMemberItemStacks();
        } else {
            return List.of(sign.toItemStack());
        }
    }

    /**
     * Returns the number of signs in this entry.
     */
    public int getSignCount() {
        return isGroup() ? group.getMemberSigns().size() : 1;
    }

    /**
     * Returns the plain text content for search matching.
     */
    public String getPlainText() {
        return isGroup() ? group.getPlainText() : sign.getPlainText();
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var displaySign = getDisplaySign();
        var material = displaySign.getSignMaterial();

        // 1. Sign type indicator: colored unicode block with hover showing material
        var blockChar = getBlockCharForSign(displaySign);
        var blockColor = getColorForMaterial(material);
        var typeIndicator = Component.text(blockChar, blockColor)
                .hoverEvent(HoverEvent.showText(Component.text(displaySign.getSignTypeDescription(), NamedTextColor.GRAY)));

        // 2. Count bracket: [N] for groups, [1] for singles
        var countText = Component.text(" [" + getSignCount() + "] ", NamedTextColor.GRAY);

        // 3. Name with hover (sign content) and click action (give signs)
        var hoverContent = Component.text();
        var hoverLines = displaySign.buildHoverLines();
        for (int i = 0; i < hoverLines.size(); i++) {
            if (i > 0) hoverContent.appendNewline();
            hoverContent.append(hoverLines.get(i));
        }
        if (isGroup()) {
            hoverContent.appendNewline();
            hoverContent.append(Component.text("Group: " + group.getName() + " (" + getSignCount() + " signs)", NamedTextColor.YELLOW));
        }
        hoverContent.appendNewline();
        hoverContent.append(Component.text("Click to receive", NamedTextColor.GREEN));

        var nameComponent = Component.text(getName(), paginator.getHighlightColor())
                .hoverEvent(HoverEvent.showText(hoverContent.build()))
                .clickEvent(ClickEvent.runCommand("/sm give " + getName()));

        var line = Component.text();
        line.append(typeIndicator);
        line.append(countText);
        line.append(nameComponent);

        return line.build();
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return getName();
    }

    /**
     * Returns the appropriate unicode block character for the sign type.
     * Full block for regular signs, lower half for hanging signs.
     */
    private static String getBlockCharForSign(SavedSign sign) {
        if (sign.isHangingSign()) {
            return BLOCK_THREE_QUARTER;
        }
        return BLOCK_FULL;
    }

    /**
     * Returns a hex color for the sign material type.
     * Colors approximate the wood type's appearance.
     * These are placeholder values — the user will set exact colors manually.
     */
    private static TextColor getColorForMaterial(Material material) {
        var name = material.name();
        if (name.startsWith("OAK")) return TextColor.color(0xaa8a61);
        if (name.startsWith("SPRUCE")) return TextColor.color(0x705538);
        if (name.startsWith("BIRCH")) return TextColor.color(0xc4c4c4);
        if (name.startsWith("JUNGLE")) return TextColor.color(0x494949);
        if (name.startsWith("ACACIA")) return TextColor.color(0x4b4a4a);
        if (name.startsWith("DARK_OAK")) return TextColor.color(0x374d2c);
        if (name.startsWith("MANGROVE")) return TextColor.color(0xaa4334);
        if (name.startsWith("CHERRY")) return TextColor.color(0x909090);
        if (name.startsWith("BAMBOO")) return TextColor.color(0x2c2c2c);
        if (name.startsWith("CRIMSON")) return TextColor.color(0xce7533);
        if (name.startsWith("WARPED")) return TextColor.color(0x5787a2);
        if (name.startsWith("PALE_OAK")) return TextColor.color(0xc7a840);
        return TextColor.color(0xC4A054); // default to oak-ish
    }
}
