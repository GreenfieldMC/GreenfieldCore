package net.greenfieldmc.core.signmanager;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.IVaultService;
import net.greenfieldmc.core.shared.services.VaultServiceImpl;
import net.greenfieldmc.core.signmanager.services.ISignManagerService;
import net.greenfieldmc.core.signmanager.services.ISignManagerStorageService;
import net.greenfieldmc.core.signmanager.services.SignManagerCommandService;
import net.greenfieldmc.core.signmanager.services.SignManagerServiceImpl;
import net.greenfieldmc.core.signmanager.services.SignManagerStorageServiceImpl;

import java.util.function.Predicate;

public class SignManagerModule extends Module {

    private IVaultService vaultService;
    private ISignManagerStorageService storageService;
    private ISignManagerService signManagerService;

    public SignManagerModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        vaultService = enableIntegration(new VaultServiceImpl(plugin, this), true);
        storageService = enableIntegration(new SignManagerStorageServiceImpl(plugin, this), true);
        signManagerService = enableIntegration(new SignManagerServiceImpl(plugin, this, storageService), true);
        enableIntegration(new SignManagerCommandService(plugin, this, signManagerService), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(signManagerService);
        disableIntegration(storageService);
        disableIntegration(vaultService);
    }
}
