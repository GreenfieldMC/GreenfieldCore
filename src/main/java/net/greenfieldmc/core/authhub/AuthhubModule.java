package net.greenfieldmc.core.authhub;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.authhub.services.AuthhubIntegrationService;
import net.greenfieldmc.core.authhub.services.AuthhubServiceImpl;
import net.greenfieldmc.core.authhub.services.IAuthhubService;

public class AuthhubModule extends Module {

    public AuthhubModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isAuthHubEnabled);
    }

    @Override
    protected IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IAuthhubService.class, AuthhubServiceImpl.class)
                .addSingleton(AuthhubIntegrationService.class, AuthhubIntegrationService.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() {
    }

    @Override
    protected void tryDisable() {
    }
}
