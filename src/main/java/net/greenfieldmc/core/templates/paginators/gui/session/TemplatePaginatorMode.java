package net.greenfieldmc.core.templates.paginators.gui.session;

/**
 * Mode enum for the template paginator GUI.
 * Determines the behavior when players interact with template items.
 */
public enum TemplatePaginatorMode {
    
    /** Browse and select templates - can copy to clipboard or get template items */
    SELECT,
    
    /** Modify brush templates - add/remove templates from the active brush */
    BRUSH,
    
    /** Manage owned template items in player's inventory */
    INVENTORY
}

