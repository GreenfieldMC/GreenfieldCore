package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class UtilitiesModule extends Module {

    public UtilitiesModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        new UtilityCommands(plugin);
    }

    @Override
    public void onDisable() {

    }
}
