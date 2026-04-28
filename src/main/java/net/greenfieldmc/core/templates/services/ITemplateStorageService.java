package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Tag;
import net.greenfieldmc.core.templates.models.Template;

import java.util.List;
import java.util.function.Predicate;

public interface ITemplateStorageService extends IModuleService<ITemplateStorageService> {

    // ---- Template methods ----

    /** Get a template by name, or null if not found. */
    Template getTemplate(String name);

    /** Get all templates. */
    default List<Template> getTemplates() {
        return getTemplates(template -> true);
    }

    /** Get all templates matching the given predicate. */
    List<Template> getTemplates(Predicate<Template> filter);

    /** Save (create or overwrite) a template. */
    void saveTemplate(Template template);

    /** Delete a template by name. */
    void deleteTemplate(String name);

    // ---- Tag methods ----

    /** Get a tag by name, or null if not found. */
    Tag getTag(String name);

    /** Get all tags. */
    List<Tag> getTags();

    /** Save (create or overwrite) a tag. */
    void saveTag(Tag tag);

    /** Delete a tag by name. */
    void deleteTag(String name);

    // ---- Persistence ----

    /** Flush all data to disk. */
    void saveDatabase();

}


