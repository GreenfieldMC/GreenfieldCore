package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Tag;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ITemplateService extends IModuleService<ITemplateService> {

    /**
     * Get the template session for the given UUID
     * @param uuid the UUID of the session
     * @return the template session for the given UUID
     */
    TemplateSession getSession(UUID uuid);

    /**
     * Create a new template session for the given UUID
     * @param uuid the UUID of the session
     * @return the new template session for the given UUID
     */
    TemplateSession createSession(UUID uuid);

    /**
     * Get all templates
     * @return a list of all templates
     */
    List<Template> getTemplates();

    /**
     * Get all templates that match the given filter
     * @param filter the filter to apply to the templates
     * @return a list of all templates that match the filter
     */
    List<Template> getTemplates(Predicate<Template> filter);

    /**
     * Get a template by name
     * @param name the name of the template
     * @return the template with the given name. If no template is found, null is returned
     */
    Template getTemplate(String name);

    /**
     * Create a new template
     * @param templateName the name of the template
     * @param schematicFile the schematic file for this template
     * @param attributes the attributes for this template
     * @return the new template
     */
    Template createTemplate(String templateName, String schematicFile, List<String> attributes);

    /**
     * Create a new template with a display item
     * @param templateName the name of the template
     * @param schematicFile the schematic file for this template
     * @param attributes the attributes for this template
     * @param displayItem the item to represent this template in the GUI
     * @return the new template
     */
    Template createTemplate(String templateName, String schematicFile, List<String> attributes, @Nullable ItemStack displayItem);

    /**
     * Update a template
     * @param templateToUpdate the template to update
     * @param templateName the new name of the template
     * @param schematicFile the new schematic file for this template
     * @param attributes the new attributes for this template
     * @return the updated template
     */
    Template updateTemplate(@NotNull Template templateToUpdate, @Nullable String templateName, @Nullable String schematicFile, @Nullable List<String> attributes);

    /**
     * Update the display item for a template
     * @param template the template to update
     * @param displayItem the new display item
     * @return the updated template
     */
    Template updateDisplayItem(@NotNull Template template, @NotNull ItemStack displayItem);

    /**
     * Delete a template
     * @param templateToDelete the template to delete
     * @return the deleted template
     */
    Template deleteTemplate(@NotNull Template templateToDelete);

    /**
     * Create a new template brush with no templates or options included by default.
     * @param forUser the user to create the brush for
     * @return a new template brush
     */
    TemplateBrush createBrush(UUID forUser);

    /**
     * Update a template brush
     * @param forUser the user to update the brush for
     * @param updatedBrush the updated template brush
     */
    void updateBrush(UUID forUser, TemplateBrush updatedBrush);

    /**
     * Start placement mode for the given player, loading the template if needed.
     * The template model will follow the player's crosshair at 1:1 scale.
     *
     * @param player the player to start placement mode for
     * @param template the template to place
     * @param ignoreSizeLimit whether to bypass the size limit check
     * @param onStart callback when placement mode starts. Exception is passed if it failed, null on success
     * @param onConfirm callback invoked with the anchor location and rotation degrees when the player right-clicks to confirm placement
     */
    void startPlacementMode(Player player, Template template, boolean ignoreSizeLimit, Consumer<Exception> onStart, BiConsumer<Location, Integer> onConfirm);

    /**
     * Cancel placement mode for the given player
     * @param player the player to cancel placement for
     * @return true if placement was active and cancelled, false otherwise
     */
    boolean cancelPlacementMode(Player player);

    /**
     * Confirm placement mode for the given player and return the anchor location
     * @param player the player to confirm placement for
     * @return the anchor location where the template was placed, or null if not in placement mode
     */
    Location confirmPlacementMode(Player player);

    /**
     * Check if the player is currently in placement mode
     * @param player the player to check
     * @return true if in placement mode
     */
    boolean isInPlacementMode(Player player);

    // ---- Tag methods ----

    /** Get a tag by name, or null. */
    Tag getTag(String name);

    /** Get all tags. */
    List<Tag> getTags();

    /**
     * Create or update a tag.
     * @param name the tag name (should match a template attribute)
     * @param displayItem the item to show in the filter panel for this tag
     * @return the saved tag
     */
    Tag saveTag(String name, ItemStack displayItem);

    /**
     * Delete a tag by name.
     * @param name the tag to delete
     * @return true if the tag existed and was removed
     */
    boolean deleteTag(String name);

    // ---- Paste preferences ----

    /**
     * Returns whether the given player has the "paste ignore air" option enabled.
     * Defaults to {@code true} if the player has no session yet.
     */
    boolean isPasteIgnoreAir(UUID playerUuid);

    /**
     * Sets the "paste ignore air" preference for the given player.
     * Creates a session lazily if one does not already exist.
     */
    void setPasteIgnoreAir(UUID playerUuid, boolean ignoreAir);

}
