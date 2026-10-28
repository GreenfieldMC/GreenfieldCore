package net.greenfieldmc.core.paintingswitch;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.paintingswitch.services.IPaintingSwitchService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchCommandService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchServiceImpl;

public class PaintingSwitchModule extends Module {

    public PaintingSwitchModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isPaintingSwitchEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IPaintingSwitchService.class, PaintingSwitchServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(PaintingSwitchCommandService.class);
    }

    @Override
    public void tryDisable() {
    }

}
