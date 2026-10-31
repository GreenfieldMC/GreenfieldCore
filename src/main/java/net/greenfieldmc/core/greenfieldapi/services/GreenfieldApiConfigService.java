package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;

/**
 * Implementation of the Greenfield API configuration service.
 * Manages API URL, client ID, and client secret from configuration file.
 */
public class GreenfieldApiConfigService implements IGreenfieldApiConfigService {

    private IConfig config;
    private String apiUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;

    public GreenfieldApiConfigService(IGreenfieldCorePlugin plugin) {
        try {
            this.config = ConfigType.YML.createNew(plugin, "greenfield-api");

            // Set default values
            config.addEntry("api.url", "https://dev-api.greenfieldmc.net/api/v1.0");
            config.addEntry("api.clientId", "your-client-id-here");
            config.addEntry("api.clientSecret", "your-client-secret-here");
            config.addEntry("api.redirectUrl", "http://localhost/callback");

            // Load values from config
            this.apiUrl = config.getString("api.url");
            this.clientId = config.getString("api.clientId");
            this.clientSecret = config.getString("api.clientSecret");
            this.redirectUrl = config.getString("api.redirectUrl");

            // Validate configuration
            if (clientId.equals("your-client-id-here") || clientSecret.equals("your-client-secret-here")) {
                plugin.getLogger().warning("Greenfield API credentials not configured! Please update greenfield-api.yml");
            }

            config.save();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load GreenfieldApiConfigService", e);
        }
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }
}

