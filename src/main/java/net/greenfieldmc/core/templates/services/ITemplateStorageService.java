package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;

import java.util.List;

public interface ITemplateStorageService extends IModuleService<ITemplateStorageService> {

    /**
     * Get a template by name
     * @param name the name of the template
     * @return the template with the given name. If no template is found, null is returned
     */
    Template getTemplate(String name);

    /**
     * Get all templates
     * @return a list of all templates
     */
    List<Template> getTemplates();

    /**
     * Save a template to the database. Will overwrite the existing template if it exists
     * @param template the template to save
     */
    void saveTemplate(Template template);

    /**
     * Delete a template by name
     * @param name the name of the template
     */
    void deleteTemplate(String name);

    /**
     * Save the templates to the database
     */
    void saveDatabase();

}
