package net.greenfieldmc.core.authhub;

import com.njdaeger.pdk.serviceprovider.ServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.authhub.services.AuthhubIntegrationService;
import net.greenfieldmc.core.authhub.services.AuthhubServiceImpl;
import net.greenfieldmc.core.greenfieldapi.GreenfieldApiModule;

public class AuthhubModule extends Module {

    private AuthhubIntegrationService authhubIntegrationService;
    private final GreenfieldApiModule greenfieldCoreApi;

    public AuthhubModule(IGreenfieldCorePlugin plugin, GreenfieldApiModule greenfieldCoreApi) {
        super(plugin, ModuleConfig::isAuthHubEnabled);
        this.greenfieldCoreApi = greenfieldCoreApi;
    }

    @Override
    protected ServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) throws Exception {
        var authHubService = enableIntegration(new AuthhubServiceImpl(plugin, this), true);
        authhubIntegrationService = enableIntegration(new AuthhubIntegrationService(plugin, this, authHubService, greenfieldCoreApi.getUserApiService()), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        disableIntegration(authhubIntegrationService);

    }
}
