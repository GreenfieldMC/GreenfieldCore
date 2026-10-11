package net.greenfieldmc.core.commandstore;

import com.njdaeger.pdk.serviceprovider.ServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.commandstore.services.CommandStoreCommandService;
import net.greenfieldmc.core.commandstore.services.CommandStoreServiceImpl;
import net.greenfieldmc.core.commandstore.services.ICommandStoreService;

public class CommandStoreModule extends Module {

    private ICommandStoreService commandStoreService;
    
    public CommandStoreModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isCommandStoreEnabled);
    }
    
    @Override
    public ServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        this.commandStoreService = enableIntegration(new CommandStoreServiceImpl(plugin, this), true);
        enableIntegration(new CommandStoreCommandService(plugin, this, commandStoreService), true);
    }
    
    @Override
    public void tryDisable() {
        disableIntegration(commandStoreService);
    }
}
