package net.greenfieldmc.core.authhub;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.authhub.services.AuthhubIntegrationService;
import net.greenfieldmc.core.authhub.services.AuthhubServiceImpl;
import net.greenfieldmc.core.greenfieldapi.GreenfieldApiModule;

import java.util.function.Predicate;

public class AuthhubModule extends Module {

    private AuthhubIntegrationService authhubIntegrationService;
    private final GreenfieldApiModule greenfieldCoreApi;

    public AuthhubModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable, GreenfieldApiModule greenfieldCoreApi) {
        super(plugin, canEnable);
        this.greenfieldCoreApi = greenfieldCoreApi;
    }

    @Override
    protected void tryEnable() throws Exception {
        var authHubService = enableIntegration(new AuthhubServiceImpl(plugin, this), true);
        authhubIntegrationService = enableIntegration(new AuthhubIntegrationService(plugin, this, authHubService, greenfieldCoreApi.getUserApiService()), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(authhubIntegrationService);

    }
}
