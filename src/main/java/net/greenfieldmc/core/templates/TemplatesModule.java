package net.greenfieldmc.core.templates;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateStorageService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.greenfieldmc.core.templates.services.ITemplateWorldEditService;
import net.greenfieldmc.core.templates.services.impl.TemplateCommandService;
import net.greenfieldmc.core.templates.services.impl.TemplateService;
import net.greenfieldmc.core.templates.services.impl.TemplateStorageService;
import net.greenfieldmc.core.templates.services.impl.TemplateViewerService;
import net.greenfieldmc.core.templates.services.impl.TemplateWorldEditService;

import java.util.function.Predicate;

public class TemplatesModule extends Module {

    private ITemplateViewerService viewerService;
    private ITemplateStorageService storageService;
    private ITemplateService templateService;
    private IWorldEditService worldEditService;
    private TemplateCommandService commandService;

    public TemplatesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        this.storageService = enableIntegration(new TemplateStorageService(plugin, this), true);
        this.templateService = enableIntegration(new TemplateService(plugin, this, storageService, null), true);
        this.viewerService = enableIntegration(new TemplateViewerService(plugin, this, templateService), true);
        // Now update templateService with viewerService reference
        ((TemplateService) templateService).setViewerService(viewerService);
        this.worldEditService = enableIntegration(new TemplateWorldEditService(plugin, this, templateService), true);
        this.commandService = enableIntegration(new TemplateCommandService(plugin, this, templateService, (ITemplateWorldEditService) worldEditService, viewerService), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(commandService);
        disableIntegration(worldEditService);
        disableIntegration(templateService);
        disableIntegration(storageService);
        disableIntegration(viewerService);
    }
}
