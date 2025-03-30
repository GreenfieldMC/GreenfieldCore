package net.greenfieldmc.core.authhub;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.authhub.services.AuthhubIntegrationService;
import net.greenfieldmc.core.authhub.services.AuthhubServiceImpl;
import net.greenfieldmc.core.shared.services.VaultServiceImpl;

import java.util.function.Predicate;

public class AuthhubModule extends Module {

    private AuthhubIntegrationService authhubIntegrationService;

    public AuthhubModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        var vaultService = enableIntegration(new VaultServiceImpl(plugin, this), false);
        var authHubService = enableIntegration(new AuthhubServiceImpl(plugin, this), true);
        authhubIntegrationService = enableIntegration(new AuthhubIntegrationService(plugin, this, vaultService, authHubService), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(authhubIntegrationService);

    }
}
