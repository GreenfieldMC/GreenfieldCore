package net.greenfieldmc.core.templates.services.impl;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.TemplateMessages;
import net.greenfieldmc.core.templates.models.Tag;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.models.TemplateSession;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateStorageService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TemplateService extends ModuleService<ITemplateService> implements ITemplateService {

    private final Map<UUID, TemplateSession> sessions = new HashMap<>();
    private final ITemplateStorageService storageService;
    private ITemplateViewerService viewerService;

    public TemplateService(Plugin plugin, Module module, ITemplateStorageService storageService, @Nullable ITemplateViewerService viewerService) {
        super(plugin, module);
        this.storageService = storageService;
        this.viewerService = viewerService;
    }

    /**
     * Set the viewer service after construction (for dependency injection).
     */
    public void setViewerService(ITemplateViewerService viewerService) {
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
        return createTemplate(templateName, schematicFile, attributes, null);
    }

    @Override
    public Template createTemplate(String templateName, String schematicFile, List<String> attributes, @Nullable ItemStack displayItem) {
        Template template = new Template(templateName, schematicFile, attributes, displayItem);
        storageService.saveTemplate(template);
        storageService.saveDatabase();
        return template;
    }

    @Override
    public Template updateDisplayItem(@NotNull Template template, @NotNull ItemStack displayItem) {
        template.setDisplayItem(displayItem);
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
    public void startPlacementMode(Player player, Template template, boolean ignoreSizeLimit, Consumer<Exception> onStart, BiConsumer<Location, Integer> onConfirm) {
        if (viewerService.isInPlacementMode(player)) viewerService.cancelPlacement(player);

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (!template.isLoaded()) {
                try {
                    template.loadClipboard();
                } catch (Exception e) {
                    onStart.accept(e);
                    return;
                }
            }

            if (!template.isLoaded()) {
                onStart.accept(new Exception(TemplateMessages.ERROR_TEMPLATE_NOT_LOADED));
                return;
            }

            if (!ignoreSizeLimit && template.getBlockCount() > 250000) {
                onStart.accept(new Exception(TemplateMessages.ERROR_TEMPLATE_TOO_LARGE));
                return;
            }

            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                viewerService.startPlacementMode(player, template, onConfirm);
                onStart.accept(null);
            });
        });
    }

    @Override
    public boolean cancelPlacementMode(Player player) {
        if (!viewerService.isInPlacementMode(player)) return false;
        viewerService.cancelPlacement(player);
        return true;
    }

    @Override
    public Location confirmPlacementMode(Player player) {
        if (!viewerService.isInPlacementMode(player)) return null;
        return viewerService.confirmPlacement(player);
    }

    @Override
    public boolean isInPlacementMode(Player player) {
        return viewerService.isInPlacementMode(player);
    }

    // ---- Tag methods ----

    @Override
    public Tag getTag(String name) {
        return storageService.getTag(name);
    }

    @Override
    public List<Tag> getTags() {
        return storageService.getTags();
    }

    @Override
    public Tag saveTag(String name, ItemStack displayItem) {
        var existing = storageService.getTag(name);
        Tag tag = existing != null ? existing : new Tag(name, displayItem);
        tag.setDisplayItem(displayItem);
        storageService.saveTag(tag);
        storageService.saveDatabase();
        return tag;
    }

    @Override
    public boolean deleteTag(String name) {
        if (storageService.getTag(name) == null) return false;
        storageService.deleteTag(name);
        storageService.saveDatabase();
        return true;
    }

    @Override
    public boolean isPasteIgnoreAir(UUID playerUuid) {
        // Load from persistent storage (default true)
        boolean stored = storageService.getPlayerBoolean(playerUuid, "pasteIgnoreAir", true);
        
        // Update in-memory session if it exists
        var session = sessions.get(playerUuid);
        if (session != null) {
            session.setPasteIgnoreAir(stored);
        }
        
        return stored;
    }

    @Override
    public void setPasteIgnoreAir(UUID playerUuid, boolean ignoreAir) {
        // Save to persistent storage
        storageService.setPlayerBoolean(playerUuid, "pasteIgnoreAir", ignoreAir);
        storageService.savePlayerConfig(playerUuid);
        
        // Update in-memory session
        var session = sessions.get(playerUuid);
        if (session == null) {
            session = new TemplateSession(playerUuid);
            sessions.put(playerUuid, session);
        }
        session.setPasteIgnoreAir(ignoreAir);
    }
}
