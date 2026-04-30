package net.greenfieldmc.core.templates.paginators.gui;

/**
 * Layout constants for the template GUI paginator.
 * Defines slot positions and sizes for both the chest inventory and player inventory.
 */
public final class TemplatePaginatorConstants {

    // =========================================================
    //  Chest (top inventory) layout
    // =========================================================
    
    /** Total size of the chest inventory */
    public static final int CHEST_SIZE = 54;
    
    /** Number of template items shown per page (rows 1-5) */
    public static final int ITEMS_PER_PAGE = 45;
    
    /** Slot for "Previous Page" button */
    public static final int SLOT_PREV = 45;
    
    /** Slot for "Paste Ignore Air" toggle */
    public static final int SLOT_PASTE_AIR = 47;
    
    /** Slot for info/page indicator */
    public static final int SLOT_INFO = 49;
    
    /** Slot for "Random Rotation" toggle */
    public static final int SLOT_RANDOM_ROTATION = 51;
    
    /** Slot for "Next Page" button */
    public static final int SLOT_NEXT = 53;

    // =========================================================
    //  Player inventory layout
    // =========================================================
    
    /** Number of tag filter items shown per page */
    public static final int TAGS_PER_PAGE = 18;
    
    /** First player inventory slot used for tag items */
    public static final int TAG_SLOT_START = 9;
    
    /** Last player inventory slot used for tag items */
    public static final int TAG_SLOT_END = 26;
    
    /** Hotbar slot for "Previous Tags Page" button */
    public static final int HOTBAR_PREV = 27;
    
    /** Hotbar slot for "Clear All Filters" button */
    public static final int HOTBAR_CLEAR = 28;
    
    /** Hotbar slot for tag info item */
    public static final int HOTBAR_INFO = 31;
    
    /** Hotbar slot for "Next Tags Page" button */
    public static final int HOTBAR_NEXT = 35;

    // Utility class - prevent instantiation
    private TemplatePaginatorConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

