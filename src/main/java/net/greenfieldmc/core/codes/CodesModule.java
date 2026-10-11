package net.greenfieldmc.core.codes;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.codes.services.CodesCommandService;
import net.greenfieldmc.core.codes.services.CodesServiceImpl;
import net.greenfieldmc.core.codes.services.ICodesService;

public class CodesModule extends Module {

    public CodesModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isCodesEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(ICodesService.class, CodesServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(CodesCommandService.class);
    }

    @Override
    public void tryDisable() {
    }

}
