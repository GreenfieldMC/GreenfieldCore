package net.greenfieldmc.core.utilities;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.utilities.services.UtilityCommands;
import net.greenfieldmc.core.utilities.services.WorldEditPrefsService;

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
