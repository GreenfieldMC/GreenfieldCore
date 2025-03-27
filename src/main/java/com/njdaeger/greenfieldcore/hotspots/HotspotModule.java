package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.hotspots.services.HotspotCommandService;
import com.njdaeger.greenfieldcore.hotspots.services.HotspotDynmapService;
import com.njdaeger.greenfieldcore.hotspots.services.HotspotServiceImpl;
import com.njdaeger.greenfieldcore.hotspots.services.HotspotStorageServiceImpl;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotService;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotStorageService;
import com.njdaeger.greenfieldcore.shared.services.EssentialsServiceImpl;
import com.njdaeger.greenfieldcore.shared.services.IDynmapService;
import com.njdaeger.greenfieldcore.shared.services.IEssentialsService;

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
