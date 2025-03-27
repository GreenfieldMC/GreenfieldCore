package com.njdaeger.greenfieldcore.authhub;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.authhub.services.AuthhubIntegrationService;
import com.njdaeger.greenfieldcore.authhub.services.AuthhubServiceImpl;
import com.njdaeger.greenfieldcore.shared.services.VaultServiceImpl;

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
