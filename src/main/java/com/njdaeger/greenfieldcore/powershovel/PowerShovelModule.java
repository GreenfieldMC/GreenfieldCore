package com.njdaeger.greenfieldcore.powershovel;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.shared.services.CoreProtectServiceImpl;
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
