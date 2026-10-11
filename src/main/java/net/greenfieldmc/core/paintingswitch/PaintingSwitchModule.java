package net.greenfieldmc.core.paintingswitch;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.paintingswitch.services.IPaintingSwitchService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchCommandService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchServiceImpl;
import org.bukkit.event.Listener;

public class PaintingSwitchModule extends Module implements Listener {

    public PaintingSwitchModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isPaintingSwitchEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(IPaintingSwitchService.class, PaintingSwitchServiceImpl.class)
                .addSingleton(PaintingSwitchCommandService.class, PaintingSwitchCommandService.class)
                .build(plugin);
    }

    @Override
    public void tryDisable() {
    }

}
