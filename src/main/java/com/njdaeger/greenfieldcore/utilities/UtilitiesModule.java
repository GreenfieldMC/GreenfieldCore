package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;

import java.util.function.Predicate;

public class UtilitiesModule extends Module {

    public UtilitiesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        enableIntegration(new UtilityCommands(plugin, this), true);
        enableIntegration(new WorldEditPrefsService(plugin, this), false);
    }

    @Override
    public void tryDisable() {

    }

}
