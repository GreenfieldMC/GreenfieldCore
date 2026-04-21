package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IModuleService;

/**
 * Service interface for managing Greenfield API configuration.
 */
public interface IGreenfieldApiConfigService extends IModuleService<IGreenfieldApiConfigService> {

    /**
     * Gets the base URL for the Greenfield API.
     * @return The API base URL
     */
    String getApiUrl();

    /**
     * Gets the OAuth2 client ID for authentication.
     * @return The client ID
     */
    String getClientId();

    /**
     * Gets the OAuth2 client secret for authentication.
     * @return The client secret
     */
    String getClientSecret();

    /**
     * Gets the redirect url used for connecting services to the user account.
     * @return The redirect URL
     */
    String getRedirectUrl();
}

