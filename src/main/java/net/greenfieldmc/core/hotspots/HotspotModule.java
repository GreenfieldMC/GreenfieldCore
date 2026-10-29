package net.greenfieldmc.core.hotspots;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.hotspots.services.HotspotCommandService;
import net.greenfieldmc.core.hotspots.services.HotspotDynmapLoader;
import net.greenfieldmc.core.hotspots.services.HotspotServiceImpl;
import net.greenfieldmc.core.hotspots.services.HotspotStorageServiceImpl;
import net.greenfieldmc.core.hotspots.services.IHotspotService;
import net.greenfieldmc.core.hotspots.services.IHotspotStorageService;

public class HotspotModule extends Module {

    public HotspotModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isHotspotsEnabled);
    }

    @Override
    protected IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IHotspotStorageService.class, HotspotStorageServiceImpl.class)
                .addSingleton(IHotspotService.class, HotspotServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(HotspotDynmapLoader.class);
        moduleServiceProvider.initialize(HotspotCommandService.class);
    }

    @Override
    protected void tryDisable() {
    }

}
