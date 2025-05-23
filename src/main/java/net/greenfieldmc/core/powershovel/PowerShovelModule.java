package net.greenfieldmc.core.powershovel;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;
import org.bukkit.event.Listener;

import java.util.function.Predicate;

public class PowerShovelModule extends Module implements Listener {

    public PowerShovelModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        var coreprotectService = enableIntegration(new CoreProtectServiceImpl(plugin, this), false);
        var worldEditService = enableIntegration(new WorldEditServiceImpl(plugin, this), true);
        enableIntegration(new PowerShovelService(plugin, this, coreprotectService, worldEditService), true);
    }

    @Override
    public void tryDisable() {

    }

}
