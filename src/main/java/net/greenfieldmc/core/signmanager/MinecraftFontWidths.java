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

    // Space character width (4px glyph + 1px gap = 5px effective)
    private static final int SPACE_EFFECTIVE_WIDTH = 4;

    // Default character width if not in the map (6px is the most common width)
    private static final int DEFAULT_CHAR_WIDTH = 6;

    // Map of character -> glyph pixel width (not including the 1px inter-char gap)
    private static final Map<Character, Integer> CHAR_WIDTHS = new HashMap<>();

    static {
        // Width 1
        for (char c : "!,.:;|iì".toCharArray()) CHAR_WIDTHS.put(c, 2);

        // Width 2 (glyph 2px)
        CHAR_WIDTHS.put('\'', 2);
        CHAR_WIDTHS.put('l', 3);
        CHAR_WIDTHS.put('`', 2);

        // Width 3
        for (char c : "\"()*Iïî[]{}".toCharArray()) CHAR_WIDTHS.put(c, 4);
        CHAR_WIDTHS.put('t', 4);

        // Width 4
        for (char c : " <>fkÎ".toCharArray()) CHAR_WIDTHS.put(c, 4);

        // Width 5 (most common, this is the default)
        // a-z (except f,i,k,l,t), A-Z (except I,M,W), 0-9, many symbols
        // We handle these via the default

        // Width 6
        for (char c : "@~®©".toCharArray()) CHAR_WIDTHS.put(c, 7);

        // Bold adds 1 pixel to every character - not handled here as lore is typically not bold

        // Specific overrides for common characters
        CHAR_WIDTHS.put(' ', 4);
        CHAR_WIDTHS.put('i', 2);
        CHAR_WIDTHS.put('!', 2);
        CHAR_WIDTHS.put(',', 2);
        CHAR_WIDTHS.put('.', 2);
        CHAR_WIDTHS.put(':', 2);
        CHAR_WIDTHS.put(';', 2);
        CHAR_WIDTHS.put('|', 2);
        CHAR_WIDTHS.put('\'', 2);
        CHAR_WIDTHS.put('`', 2);
        CHAR_WIDTHS.put('l', 3);
        CHAR_WIDTHS.put('"', 4);
        CHAR_WIDTHS.put('(', 4);
        CHAR_WIDTHS.put(')', 4);
        CHAR_WIDTHS.put('*', 4);
        CHAR_WIDTHS.put('I', 4);
        CHAR_WIDTHS.put('[', 4);
        CHAR_WIDTHS.put(']', 4);
        CHAR_WIDTHS.put('{', 4);
        CHAR_WIDTHS.put('}', 4);
        CHAR_WIDTHS.put('t', 4);
        CHAR_WIDTHS.put('<', 5);
        CHAR_WIDTHS.put('>', 5);
        CHAR_WIDTHS.put('f', 5);
        CHAR_WIDTHS.put('k', 5);
        CHAR_WIDTHS.put('@', 7);
        CHAR_WIDTHS.put('~', 7);
    }

    /**
     * Gets the pixel width of a single character in Minecraft's default font.
     * This includes the character glyph width but NOT the 1px inter-character gap.
     */
    public static int getCharWidth(char c) {
        return CHAR_WIDTHS.getOrDefault(c, DEFAULT_CHAR_WIDTH);
    }

    /**
     * Calculates the total pixel width of a string of text in Minecraft's default font.
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
     * Creates a center-padded version of the given component for use in lore,
     * mimicking how signs render center-aligned text.
     *
     * @param component The component to center.
     * @param targetWidth The target pixel width to center within (typically SIGN_WIDTH_PIXELS).
     * @return A new component with leading spaces to approximate center alignment.
     */
    public static Component centerForLore(Component component, int targetWidth) {
        int textWidth = getComponentWidth(component);
        if (textWidth >= targetWidth) return component; // Already fills or exceeds width

        int totalPadding = targetWidth - textWidth;
        int leftPadding = totalPadding / 2;

        // Each space is SPACE_EFFECTIVE_WIDTH pixels wide (+ 1px gap if followed by text)
        int spaceCount = leftPadding / (SPACE_EFFECTIVE_WIDTH + 1);

        if (spaceCount <= 0) return component;

        var padding = Component.text(" ".repeat(spaceCount));
        return padding.append(component);
    }

    /**
     * Centers a component for lore display, using the standard sign width.
     */
    public static Component centerForLore(Component component) {
        return centerForLore(component, SIGN_WIDTH_PIXELS);
    }
}

