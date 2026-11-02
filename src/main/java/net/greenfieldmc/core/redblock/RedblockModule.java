package net.greenfieldmc.core.redblock;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.redblock.services.IRedblockConfigService;
import net.greenfieldmc.core.redblock.services.IRedblockServiceV2;
import net.greenfieldmc.core.redblock.services.IRedblockStorageService;
import net.greenfieldmc.core.redblock.services.RedblockCommandServiceV2;
import net.greenfieldmc.core.redblock.services.RedblockConfigService;
import net.greenfieldmc.core.redblock.services.RedblockDynmapLoader;
import net.greenfieldmc.core.redblock.services.RedblockListenerService;
import net.greenfieldmc.core.redblock.services.RedblockServiceV2Impl;
import net.greenfieldmc.core.redblock.services.RedblockStorageServiceImpl;

public class RedblockModule extends Module {

    public RedblockModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isRedblockEnabled);
    }

    @Override
    protected IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IRedblockConfigService.class, RedblockConfigService.class)
                .addSingleton(RedblockDynmapLoader.class, RedblockDynmapLoader.class)
                .addSingleton(IRedblockServiceV2.class, RedblockServiceV2Impl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(RedblockListenerService.class);
        moduleServiceProvider.initialize(RedblockCommandServiceV2.class);
    }

    @Override
    protected void tryDisable() {
    }
}
