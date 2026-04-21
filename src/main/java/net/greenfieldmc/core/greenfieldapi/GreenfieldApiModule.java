package net.greenfieldmc.core.greenfieldapi;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiListenerService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldCoreApiImpl;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldCoreApi;
import net.greenfieldmc.core.greenfieldapi.services.OAuthClientCredentialsHandler;

import java.util.function.Predicate;

/**
 * Module for integrating with the Greenfield Core API.
 * Provides OAuth2 authentication and async API access for user and account data.
 */
public class GreenfieldApiModule extends Module {

    private IGreenfieldApiConfigService configService;
    private OAuthClientCredentialsHandler authHandler;
    private IGreenfieldCoreApi apiService;
    private GreenfieldApiListenerService listenerService;

    public GreenfieldApiModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        // Step 1: Initialize configuration service
        this.configService = enableIntegration(new GreenfieldApiConfigService(plugin, this), true);

        // Step 2: Initialize OAuth handler with config
        this.authHandler = new OAuthClientCredentialsHandler(
                configService.getApiUrl(),
                configService.getClientId(),
                configService.getClientSecret(),
                getLogger()
        );

        // Step 3: Initialize API service with config and auth handler
        this.apiService = enableIntegration(
                new GreenfieldCoreApiImpl(plugin, this, configService, authHandler),
                true
        );

        this.listenerService = enableIntegration(new GreenfieldApiListenerService(plugin, this, apiService), true);

        getLogger().info("Greenfield API Module enabled successfully");
    }

    @Override
    protected void tryDisable() throws Exception {
        // Disable services in reverse order
        if (apiService != null) {
            disableIntegration(apiService);
        }
        if (authHandler != null) {
            authHandler.shutdown();
        }
        if (configService != null) {
            disableIntegration(configService);
        }

        if (listenerService != null) {
            disableIntegration(listenerService);
        }

        getLogger().info("Greenfield API Module disabled");
    }

    /**
     * Gets the API service for making requests to the Greenfield Core API.
     * @return The API service instance
     */
    public IGreenfieldCoreApi getApiService() {
        return apiService;
    }
}


