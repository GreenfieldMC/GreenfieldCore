package net.greenfieldmc.core.redblock;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.redblock.services.RedblockDynmapServiceImpl;
import net.greenfieldmc.core.shared.services.EssentialsServiceImpl;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import net.greenfieldmc.core.redblock.services.IRedblockService;
import net.greenfieldmc.core.redblock.services.IRedblockStorageService;
import net.greenfieldmc.core.redblock.services.RedblockCommandService;
import net.greenfieldmc.core.redblock.services.RedblockListenerService;
import net.greenfieldmc.core.redblock.services.RedblockServiceImpl;
import net.greenfieldmc.core.redblock.services.RedblockStorageServiceImpl;

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
        dynmapService = enableIntegration(new RedblockDynmapServiceImpl(plugin, this, storageService), false);
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
