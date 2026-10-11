package net.greenfieldmc.core.redblock;

import com.njdaeger.pdk.serviceprovider.ServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.redblock.services.RedblockDynmapServiceImpl;
import net.greenfieldmc.core.shared.services.EssentialsServiceImpl;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import net.greenfieldmc.core.redblock.services.IRedblockService;
import net.greenfieldmc.core.redblock.services.IRedblockStorageService;
import net.greenfieldmc.core.redblock.services.RedblockListenerService;
import net.greenfieldmc.core.redblock.services.RedblockServiceImpl;
import net.greenfieldmc.core.redblock.services.RedblockStorageServiceImpl;
import net.greenfieldmc.core.shared.services.IVaultService;
import net.greenfieldmc.core.shared.services.VaultServiceImpl;

public class RedblockModule extends Module {

    private IRedblockStorageService storageService;
    private IRedblockService redblockService;
    private IEssentialsService essentialsService;
    private IDynmapService dynmapService;
    private IVaultService vaultService;

    public RedblockModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isRedblockEnabled);
    }

    @Override
    public ServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        storageService = enableIntegration(new RedblockStorageServiceImpl(plugin, this), true);
        essentialsService = enableIntegration(new EssentialsServiceImpl(plugin, this), false);
        vaultService = enableIntegration(new VaultServiceImpl(plugin, this), false);
        dynmapService = enableIntegration(new RedblockDynmapServiceImpl(plugin, this, storageService), false);
        redblockService = enableIntegration(new RedblockServiceImpl(plugin, this, dynmapService, storageService), true);
//        enableIntegration(new RedblockCommandService(plugin, this, redblockService, essentialsService, vaultService), true);
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
