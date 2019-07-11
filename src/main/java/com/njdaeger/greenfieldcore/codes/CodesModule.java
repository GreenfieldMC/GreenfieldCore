package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class CodesModule implements Module {

    private Codes codes;
    private CodesConfig config;
    private final GreenfieldCore plugin;

    public CodesModule(GreenfieldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        config = new CodesConfig(plugin, this);
        codes = new Codes(config);

        new CodesCommand(plugin, this);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    public CodesConfig getConfig() {
        return config;
    }

    public Codes getCodes() {
        return codes;
    }

}
