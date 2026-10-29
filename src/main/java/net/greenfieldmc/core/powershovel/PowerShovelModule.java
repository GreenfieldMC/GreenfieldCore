package net.greenfieldmc.core.powershovel;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;

public class PowerShovelModule extends Module {

    public PowerShovelModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isPowerShovelEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder.build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(PowerShovelService.class);
    }

    @Override
    public void tryDisable() {

    }

}
