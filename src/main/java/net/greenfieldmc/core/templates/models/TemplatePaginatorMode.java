package net.greenfieldmc.core.templates.models;

/**
 * Mode enum for the template paginator GUI.
 * Determines the behavior when players interact with template items.
 */
public enum TemplatePaginatorMode {
    
    /** Browse and select templates - can copy to clipboard or get template items */
    SELECT,
    
    /** Modify brush templates - add/remove templates from the active brush */
    BRUSH
}

