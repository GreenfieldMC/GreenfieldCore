package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Tag;
import net.greenfieldmc.core.templates.models.Template;

import java.util.List;
import java.util.UUID;
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

    // ---- Player data methods ----

    /** Get a player preference boolean value (returns defaultValue if not set). */
    boolean getPlayerBoolean(UUID playerUuid, String key, boolean defaultValue);

    /** Set a player preference boolean value. */
    void setPlayerBoolean(UUID playerUuid, String key, boolean value);

    /** Get a player preference string value (returns defaultValue if not set). */
    String getPlayerString(UUID playerUuid, String key, String defaultValue);

    /** Set a player preference string value. */
    void setPlayerString(UUID playerUuid, String key, String value);

    /** Get a player preference integer value (returns defaultValue if not set). */
    int getPlayerInt(UUID playerUuid, String key, int defaultValue);

    /** Set a player preference integer value. */
    void setPlayerInt(UUID playerUuid, String key, int value);

    /** Save a specific player's config to disk. */
    void savePlayerConfig(UUID playerUuid);

    // ---- Main config methods ----

    /** Get the entity rendering limit for a given rank/permission group (default: 100). */
    int getEntityRenderingLimit(String rank);

    /** Set the entity rendering limit for a given rank/permission group. */
    void setEntityRenderingLimit(String rank, int limit);

    // ---- Persistence ----

    /** Flush all data to disk. */
    void saveDatabase();

}

