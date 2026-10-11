package net.greenfieldmc.core.utilities;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProvider;
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
        return serviceProviderBuilder
                .addSingleton()

        enableIntegration(new UtilityCommands(plugin, this), true);
        enableIntegration(new WorldEditPrefsService(plugin, this), false);
    }

    @Override
    public void tryDisable() {

    }

}
