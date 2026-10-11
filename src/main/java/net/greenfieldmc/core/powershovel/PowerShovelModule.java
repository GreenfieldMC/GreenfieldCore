package net.greenfieldmc.core.powershovel;

import com.njdaeger.pdk.serviceprovider.ServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;
import org.bukkit.event.Listener;

public class PowerShovelModule extends Module implements Listener {

    public PowerShovelModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isPowerShovelEnabled);
    }

    @Override
    public ServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        var coreprotectService = enableIntegration(new CoreProtectServiceImpl(plugin, this), false);
        var worldEditService = enableIntegration(new WorldEditServiceImpl(plugin, this), true);
        enableIntegration(new PowerShovelService(plugin, this, coreprotectService, worldEditService), true);
    }

    @Override
    public void tryDisable() {

    }

}
