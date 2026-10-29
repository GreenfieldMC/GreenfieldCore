package net.greenfieldmc.core.advancedbuild;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildCommandService;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildServiceImpl;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;

public class AdvancedBuildModule extends Module {

    public AdvancedBuildModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isAdvancedBuildModeEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IAdvBuildService.class, AdvBuildServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(AdvBuildCommandService.class);
    }

    @Override
    public void tryDisable() {

    }

}
