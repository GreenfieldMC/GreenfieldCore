package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
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
     * Update a template
     * @param templateToUpdate the template to update
     * @param templateName the new name of the template
     * @param schematicFile the new schematic file for this template
     * @param attributes the new attributes for this template
     * @return the updated template
     */
    Template updateTemplate(@NotNull Template templateToUpdate, @Nullable String templateName, @Nullable String schematicFile, @Nullable List<String> attributes);

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
     * Start a template view for the given player
     *
     * @param player the player to start the view for
     * @param template the template to view
     * @param scale the scale of the view
     * @param ignoreSizeLimit
     * @param onComplete the callback to call when the view is complete. Exception is passed if the view failed
     *         to start, null if it started successfully
     */
    void startTemplateView(Player player, Template template, double scale, boolean ignoreSizeLimit, Consumer<Exception> onComplete);

    /**
     * Destroy the template view for the given player
     * @param player the player to destroy the view for
     */
    boolean destroyTemplateView(Player player);

}
