package net.greenfieldmc.core.templates;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateStorageService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.greenfieldmc.core.templates.services.ITemplateWorldEditService;
import net.greenfieldmc.core.templates.services.TemplateCommandService;
import net.greenfieldmc.core.templates.services.TemplateServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateStorageServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateViewerServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateWorldEditServiceImpl;

import java.util.function.Predicate;

public class TemplatesModule extends Module {

    private ITemplateViewerService viewerService;
    private ITemplateStorageService storageService;
    private ITemplateService templateService;
    private IWorldEditService worldEditService;

    public TemplatesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        this.viewerService = enableIntegration(new TemplateViewerServiceImpl(plugin, this), true);
        this.storageService = enableIntegration(new TemplateStorageServiceImpl(plugin, this), true);
        this.templateService = enableIntegration(new TemplateServiceImpl(plugin, this, storageService, viewerService), true);
        this.worldEditService = enableIntegration(new TemplateWorldEditServiceImpl(plugin, this, templateService), true);
        enableIntegration(new TemplateCommandService(plugin, this, templateService, (ITemplateWorldEditService) worldEditService), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(worldEditService);
        disableIntegration(templateService);
        disableIntegration(storageService);
        disableIntegration(viewerService);
    }
}
