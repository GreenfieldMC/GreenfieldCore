package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

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
     * Create a new template brush with no templates or options included by default. This will need added to a session once it is built.
     */
    TemplateBrush createBrush();

}
