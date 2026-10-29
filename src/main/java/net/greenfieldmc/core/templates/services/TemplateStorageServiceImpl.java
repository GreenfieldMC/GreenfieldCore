package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.templates.models.Template;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TemplateStorageServiceImpl implements ITemplateStorageService, AutoCloseable {

    private final IConfig config;
    private final Map<String, Template> templates = new HashMap<>();

    public TemplateStorageServiceImpl(IGreenfieldCorePlugin plugin) {
        try {
            this.config = ConfigType.YML.createNew(plugin, "templates");

            if (config.hasSection("templates")) {
                for (var templateName : config.getSection("templates").getKeys(false)) {
                    var section = config.getSection("templates." + templateName);
                    var schematicFile = section.getString("schematicFile");
                    var attributes = section.getStringList("attributes");
                    templates.put(templateName, new Template(templateName, schematicFile, attributes));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to enable TemplateStorageService", e);
        }
    }

    @Override
    public void close() {
        saveDatabase();
    }

    @Override
    public Template getTemplate(String name) {
        return templates.get(name);
    }

    @Override
    public List<Template> getTemplates(Predicate<Template> filter) {
        return new ArrayList<>(templates.values()).stream()
                .filter(filter)
                .toList();
    }

    @Override
    public void saveTemplate(Template template) {
        templates.put(template.getTemplateName(), template);
        var path = "templates." + template.getTemplateName();
        config.setEntry(path + ".schematicFile", template.getSchematicFile());
        config.setEntry(path + ".attributes", template.getAttributes());
    }

    @Override
    public void deleteTemplate(String name) {
        templates.remove(name);
        config.setEntry("templates." + name, null);
    }

    @Override
    public void saveDatabase() {
        templates.values().forEach(this::saveTemplate);
        config.save();
    }
}
