package net.greenfieldmc.core.commandstore;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.commandstore.services.CommandStoreCommandService;
import net.greenfieldmc.core.commandstore.services.CommandStoreServiceImpl;
import net.greenfieldmc.core.commandstore.services.ICommandStoreService;

public class CommandStoreModule extends Module {

    public CommandStoreModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isCommandStoreEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(ICommandStoreService.class, CommandStoreServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(CommandStoreCommandService.class);
    }

    @Override
    public void tryDisable() {
    }
}
