package net.greenfieldmc.core.powershovel;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import org.bukkit.event.Listener;

import java.util.function.Predicate;

public class PowerShovelModule extends Module implements Listener {

    public PowerShovelModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        var coreprotectService = enableIntegration(new CoreProtectServiceImpl(plugin, this), false);
        enableIntegration(new PowerShovelService(plugin, this, coreprotectService), true);
    }

    @Override
    public void tryDisable() {

    }

}
