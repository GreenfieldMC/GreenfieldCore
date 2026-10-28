package net.greenfieldmc.core.utilities;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.utilities.services.UtilityCommands;
import net.greenfieldmc.core.utilities.services.WorldEditPrefsService;

public class UtilitiesModule extends Module {

    public UtilitiesModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isUtilitiesEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder.build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(UtilityCommands.class);
        try {
            moduleServiceProvider.initialize(WorldEditPrefsService.class);
        } catch (Exception e) {
            getLogger().warning("WorldEditPrefsService not enabled... " + e.getMessage());
        }
    }

    @Override
    public void tryDisable() {

    }

}
