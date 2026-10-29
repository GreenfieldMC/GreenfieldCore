package net.greenfieldmc.core.templates;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateStorageService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.greenfieldmc.core.templates.services.ITemplateWorldEditService;
import net.greenfieldmc.core.templates.services.TemplateCommandService;
import net.greenfieldmc.core.templates.services.TemplateServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateStorageServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateViewerServiceImpl;
import net.greenfieldmc.core.templates.services.TemplateWorldEditServiceImpl;

public class TemplatesModule extends Module {

    public TemplatesModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isTemplatesEnabled);
    }

    @Override
    protected IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(ITemplateViewerService.class, TemplateViewerServiceImpl.class)
                .addSingleton(ITemplateStorageService.class, TemplateStorageServiceImpl.class)
                .addSingleton(ITemplateService.class, TemplateServiceImpl.class)
                .addSingleton(ITemplateWorldEditService.class, TemplateWorldEditServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(TemplateCommandService.class);
    }

    @Override
    protected void tryDisable() {
    }
}
