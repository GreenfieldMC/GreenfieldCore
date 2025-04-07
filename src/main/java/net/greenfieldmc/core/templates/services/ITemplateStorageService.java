package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;

import java.util.List;
import java.util.function.Predicate;

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
    default List<Template> getTemplates() {
        return getTemplates(template -> true);
    }

    /**
     * Get all templates that match the given filter
     * @param filter the filter to apply to the templates
     * @return a list of all templates that match the filter
     */
    List<Template> getTemplates(Predicate<Template> filter);

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
