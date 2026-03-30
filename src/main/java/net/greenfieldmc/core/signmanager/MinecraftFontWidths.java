package net.greenfieldmc.core.signmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for calculating Minecraft text pixel widths and centering text.
 * Minecraft uses a variable-width bitmap font where each character has a
 * specific pixel width (plus 1px gap between characters).
 * Signs center-align text within a 90-pixel-wide area.
 */
public class MinecraftFontWidths {

    // Sign text rendering width in pixels
    private static final int SIGN_WIDTH_PIXELS = 90;

    // Space character effective width (glyph 4px + 1px gap)
    private static final int SPACE_EFFECTIVE_WIDTH = 5;

    // Default character width if not in the map (6px is the most common glyph width)
    private static final int DEFAULT_CHAR_WIDTH = 6;

    // Map of character -> glyph pixel width (not including the 1px inter-char gap)
    private static final Map<Character, Integer> CHAR_WIDTHS = new HashMap<>();

    static {
        // 2px glyphs
        for (char c : "!,.:;|i".toCharArray()) CHAR_WIDTHS.put(c, 2);
        CHAR_WIDTHS.put('\'', 2);
        CHAR_WIDTHS.put('`', 2);

        // 3px glyphs
        CHAR_WIDTHS.put('l', 3);

        // 4px glyphs
        for (char c : "\"()*I[]{}t".toCharArray()) CHAR_WIDTHS.put(c, 4);

        // 5px glyphs
        for (char c : "<>fk".toCharArray()) CHAR_WIDTHS.put(c, 5);
        CHAR_WIDTHS.put(' ', 4); // space glyph is 4px

        // 7px glyphs
        for (char c : "@~".toCharArray()) CHAR_WIDTHS.put(c, 7);
    }

    /**
     * Gets the pixel width of a single character glyph in Minecraft's default font.
     */
    public static int getCharWidth(char c) {
        return CHAR_WIDTHS.getOrDefault(c, DEFAULT_CHAR_WIDTH);
    }

    /**
     * Calculates the total pixel width of a string in Minecraft's default font.
     * Each character contributes its glyph width + 1px gap (except the last character).
     */
    public static int getTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += getCharWidth(text.charAt(i));
            if (i < text.length() - 1) width += 1; // 1px gap between characters
        }
        return width;
    }

    /**
     * Calculates the pixel width of a Component's plain text content.
     */
    public static int getComponentWidth(Component component) {
        var plainText = PlainTextComponentSerializer.plainText().serialize(component);
        return getTextWidth(plainText);
    }

    /**
     * Creates a center-padded version of the given component,
     * mimicking how signs render center-aligned text.
     *
     * @param component The component to center.
     * @param targetWidth The target pixel width to center within (typically SIGN_WIDTH_PIXELS).
     * @return A new component with leading spaces to approximate center alignment.
     */
    public static Component centerText(Component component, int targetWidth) {
        int textWidth = getComponentWidth(component);
        if (textWidth >= targetWidth) return component;

        int totalPadding = targetWidth - textWidth;
        int leftPadding = totalPadding / 2;

        // Each space is SPACE_EFFECTIVE_WIDTH pixels wide (4px glyph + 1px gap)
        int spaceCount = leftPadding / SPACE_EFFECTIVE_WIDTH;
        if (spaceCount <= 0) return component;

        return Component.text(" ".repeat(spaceCount)).append(component);
    }

    /**
     * Centers a component using the standard sign width.
     */
    public static Component centerText(Component component) {
        return centerText(component, SIGN_WIDTH_PIXELS);
    }
}

