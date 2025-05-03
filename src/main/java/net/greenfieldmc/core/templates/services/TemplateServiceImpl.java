package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.TemplateMessages;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TemplateServiceImpl extends ModuleService<ITemplateService> implements ITemplateService {

    private final Map<UUID, TemplateSession> sessions = new HashMap<>();
    private final ITemplateStorageService storageService;
    private final ITemplateViewerService viewerService;

    public TemplateServiceImpl(Plugin plugin, Module module, ITemplateStorageService storageService, ITemplateViewerService viewerService) {
        super(plugin, module);
        this.storageService = storageService;
        this.viewerService = viewerService;
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
            sessions.values().forEach(session -> session.getBrushes().stream().filter(brush -> brush.getTemplates().contains(templateToUpdate.getTemplateName())).forEach(brush -> {
                brush.removeTemplate(templateToUpdate.getTemplateName());
                brush.addTemplate(templateName);
            }));
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

    @Override
    public void startTemplateView(Player player, Template template, double scale, boolean ignoreSizeLimit, Consumer<Exception> onComplete) {
        if (viewerService.isTemplateViewActive(player)) viewerService.destroyTemplateView(player);

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (!template.isLoaded()) {
                try {
                    template.loadClipboard();
                } catch (Exception e) {
                    onComplete.accept(e);
                    return;
                }
            }

            if (!template.isLoaded()) {
                onComplete.accept(new Exception(TemplateMessages.ERROR_TEMPLATE_NOT_LOADED));
                return;
            }

            if (!ignoreSizeLimit && template.getBlockCount() > 32768) {
                onComplete.accept(new Exception(TemplateMessages.ERROR_TEMPLATE_TOO_LARGE));
                return;
            }

            var traceDistance = 5;
            var trace = player.rayTraceBlocks(traceDistance, FluidCollisionMode.NEVER);
            Location spawnLocation;
            if (trace == null) {
                var direction = player.getLocation().getDirection();
                spawnLocation = player.getEyeLocation().clone().add(direction.getX() * traceDistance * scale, direction.getY() * traceDistance * scale, direction.getZ() * traceDistance * scale);
            } else {
                if (scale >= 1.0) {
                    var target = trace.getHitPosition().toBlockVector();
                    spawnLocation = new Location(player.getWorld(), target.getBlockX(), target.getBlockY(), target.getBlockZ());
                } else {
                    spawnLocation = trace.getHitPosition().toLocation(player.getWorld());
                }

            }

            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                viewerService.startTemplateView(player, template, spawnLocation, scale);
                onComplete.accept(null);
            });
        });
    }

    @Override
    public boolean destroyTemplateView(Player player) {
        if (!viewerService.isTemplateViewActive(player)) return false;
        viewerService.destroyTemplateView(player);
        return true;
    }
}
