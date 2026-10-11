package net.greenfieldmc.core.advancedbuild;

import com.njdaeger.pdk.serviceprovider.ServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildCommandService;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildServiceImpl;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;

public class AdvancedBuildModule extends Module {

    private IAdvBuildService advBuildService;

    public AdvancedBuildModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isAdvancedBuildModeEnabled);
    }

    @Override
    public ServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) throws Exception {
        try {

            var worldEditService = enableIntegration(new WorldEditServiceImpl(plugin, this), false);
            var coreProtectService = enableIntegration(new CoreProtectServiceImpl(plugin, this), false);
            advBuildService = enableIntegration(new AdvBuildServiceImpl(plugin, this, worldEditService, coreProtectService), true);
            enableIntegration(new AdvBuildCommandService(plugin, this, advBuildService), true);
        } catch (Exception e) {
            throw new Exception("Failed to enable AdvancedBuildModule", e);
        }
    }

    @Override
    public void tryDisable() {
        disableIntegration(advBuildService);
    }

}
