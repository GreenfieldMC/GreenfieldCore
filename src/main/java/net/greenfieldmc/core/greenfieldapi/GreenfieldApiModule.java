package net.greenfieldmc.core.greenfieldapi;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiClientImpl;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiListenerService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldRedblockApiServiceImpl;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldUserApiServiceImpl;
import net.greenfieldmc.core.greenfieldapi.services.IAuthedClientService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldRedblockApiService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;
import net.greenfieldmc.core.greenfieldapi.services.OAuthClientCredentialsHandler;

import java.util.function.Predicate;

/**
 * Module for integrating with the Greenfield Core API.
 * Provides OAuth2 authentication and async API access for user and account data.
 */
public class GreenfieldApiModule extends Module {

    private IGreenfieldApiConfigService configService;
    private OAuthClientCredentialsHandler authHandler;
    private GreenfieldApiListenerService listenerService;
    private IGreenfieldUserApiService userApiService;
    private IGreenfieldRedblockApiService redblockApiService;
    private IAuthedClientService clientService;

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

        this.clientService = enableIntegration(new GreenfieldApiClientImpl(plugin, this, configService, authHandler), true);
        this.userApiService = enableIntegration(new GreenfieldUserApiServiceImpl(plugin, this, clientService, configService), true);
        this.redblockApiService = enableIntegration(new GreenfieldRedblockApiServiceImpl(plugin, this, clientService), true);

        this.listenerService = enableIntegration(new GreenfieldApiListenerService(plugin, this, userApiService), true);

        getLogger().info("Greenfield API Module enabled successfully");
    }

    @Override
    protected void tryDisable() throws Exception {
        // Disable services in reverse order
        if (clientService != null) {
            disableIntegration(configService);
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

        if (redblockApiService != null) {
            disableIntegration(redblockApiService);
        }
        if (userApiService != null) {
            disableIntegration(userApiService);
        }

        getLogger().info("Greenfield API Module disabled");
    }

    /**
     * Gets the API service for making requests to the Greenfield Core API.
     * @return The API service instance
     */
    public IGreenfieldUserApiService getUserApiService() {
        return userApiService;
    }
}


