package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class TemplateServiceImpl extends ModuleService<ITemplateService> implements ITemplateService {

    private final Map<UUID, TemplateSession> sessions = new HashMap<>();
    private final ITemplateStorageService storageService;

    public TemplateServiceImpl(Plugin plugin, Module module, ITemplateStorageService storageService) {
        super(plugin, module);
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public TemplateSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    @Override
    public TemplateSession createSession(UUID uuid) {
        return sessions.computeIfAbsent(uuid, TemplateSession::new);
    }

    @Override
    public List<Template> getTemplates() {
        return storageService.getTemplates();
    }

    @Override
    public List<Template> getTemplates(Predicate<Template> filter) {
        return storageService.getTemplates(filter);
    }

    @Override
    public Template getTemplate(String name) {
        return storageService.getTemplate(name);
    }

    @Override
    public Template createTemplate(String templateName, String schematicFile, List<String> attributes) {
        Template template = new Template(templateName, schematicFile, attributes);
        storageService.saveTemplate(template);
        storageService.saveDatabase();
        return template;
    }

    @Override
    public Template updateTemplate(@NotNull Template templateToUpdate, @Nullable String templateName, @Nullable String schematicFile, @Nullable List<String> attributes) {
        if (templateName != null) {
            templateToUpdate.setTemplateName(templateName);
        }
        if (schematicFile != null) {
            templateToUpdate.setSchematicFile(schematicFile);
        }
        if (attributes != null) {
            templateToUpdate.setAttributes(attributes);
        }
        storageService.saveTemplate(templateToUpdate);
        storageService.saveDatabase();
        return templateToUpdate;
    }

    @Override
    public Template deleteTemplate(@NotNull Template templateToDelete) {
        storageService.deleteTemplate(templateToDelete.getTemplateName());
        storageService.saveDatabase();
        return templateToDelete;
    }

    @Override
    public TemplateBrush createBrush(UUID forUser) {
        var session = getSession(forUser);
        if (session == null) {
            session = createSession(forUser);
        }
        var brush = new TemplateBrush();
        session.addBrush(brush);
        return brush;
    }

    @Override
    public void updateBrush(UUID forUser, TemplateBrush updatedBrush) {
        var session = getSession(forUser);
        if (session == null) {
            session = createSession(forUser);
        }
        session.updateBrush(updatedBrush);
    }
}
