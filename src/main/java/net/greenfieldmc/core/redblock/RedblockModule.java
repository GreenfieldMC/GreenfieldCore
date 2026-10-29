package net.greenfieldmc.core.redblock;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.redblock.services.IRedblockService;
import net.greenfieldmc.core.redblock.services.IRedblockStorageService;
import net.greenfieldmc.core.redblock.services.RedblockDynmapLoader;
import net.greenfieldmc.core.redblock.services.RedblockListenerService;
import net.greenfieldmc.core.redblock.services.RedblockServiceImpl;
import net.greenfieldmc.core.redblock.services.RedblockStorageServiceImpl;

public class RedblockModule extends Module {

    public RedblockModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isRedblockEnabled);
    }

    @Override
    protected IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IRedblockStorageService.class, RedblockStorageServiceImpl.class)
                .addSingleton(RedblockDynmapLoader.class, RedblockDynmapLoader.class)
                .addSingleton(IRedblockService.class, RedblockServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(RedblockListenerService.class);
    }

    @Override
    protected void tryDisable() {
    }
}
