package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class CodesModule implements Module {

    private CodesConfig config;
    private final GreenfieldCore plugin;

    public CodesModule(GreenfieldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        config = new CodesConfig(plugin);
    }

    @Override
    public void onDisable() {
    }
}
