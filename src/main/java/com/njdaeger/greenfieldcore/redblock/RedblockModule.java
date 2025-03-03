package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.redblock.services.DynmapServiceImpl;
import com.njdaeger.greenfieldcore.redblock.services.EssentialsServiceImpl;
import com.njdaeger.greenfieldcore.redblock.services.IDynmapService;
import com.njdaeger.greenfieldcore.redblock.services.IEssentialsService;
import com.njdaeger.greenfieldcore.redblock.services.IRedblockService;
import com.njdaeger.greenfieldcore.redblock.services.IRedblockStorageService;
import com.njdaeger.greenfieldcore.redblock.services.RedblockCommandService;
import com.njdaeger.greenfieldcore.redblock.services.RedblockListenerService;
import com.njdaeger.greenfieldcore.redblock.services.RedblockServiceImpl;
import com.njdaeger.greenfieldcore.redblock.services.RedblockStorageServiceImpl;

import java.util.function.Predicate;

public class RedblockModule extends Module {

    private IRedblockStorageService storageService;
    private IRedblockService redblockService;
    private IEssentialsService essentialsService;
    private IDynmapService dynmapService;

    public RedblockModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        storageService = enableIntegration(new RedblockStorageServiceImpl(plugin, this), true);
        essentialsService = enableIntegration(new EssentialsServiceImpl(plugin, this), false);
        dynmapService = enableIntegration(new DynmapServiceImpl(plugin, this, storageService), false);
        redblockService = enableIntegration(new RedblockServiceImpl(plugin, this, dynmapService, storageService), true);
        enableIntegration(new RedblockCommandService(plugin, this, redblockService, essentialsService), true);
        enableIntegration(new RedblockListenerService(plugin, this, redblockService), true);
    }

    @Override
    public void tryDisable() throws Exception {
        disableIntegration(dynmapService);
        disableIntegration(storageService);
        disableIntegration(redblockService);
        disableIntegration(essentialsService);
    }
}
