package net.greenfieldmc.core.hotspots;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.hotspots.services.HotspotCommandService;
import net.greenfieldmc.core.hotspots.services.HotspotDynmapService;
import net.greenfieldmc.core.hotspots.services.HotspotServiceImpl;
import net.greenfieldmc.core.hotspots.services.HotspotStorageServiceImpl;
import net.greenfieldmc.core.hotspots.services.IHotspotService;
import net.greenfieldmc.core.hotspots.services.IHotspotStorageService;
import net.greenfieldmc.core.shared.services.EssentialsServiceImpl;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IEssentialsService;

import java.util.function.Predicate;

public class HotspotModule extends Module {

    private IEssentialsService essentialsService;
    private IHotspotService hotspotService;
    private IDynmapService dynmapService;
    private IHotspotStorageService storage;

    public HotspotModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        storage = enableIntegration(new HotspotStorageServiceImpl(plugin, this), true);
        dynmapService = enableIntegration(new HotspotDynmapService(plugin, this, storage), false);
        hotspotService = enableIntegration(new HotspotServiceImpl(plugin, this, dynmapService, storage), true);
        essentialsService = enableIntegration(new EssentialsServiceImpl(plugin, this), false);
        enableIntegration(new HotspotCommandService(plugin, this, dynmapService, hotspotService, essentialsService), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(dynmapService);
        disableIntegration(hotspotService);
        disableIntegration(essentialsService);
        disableIntegration(storage);
    }

}
