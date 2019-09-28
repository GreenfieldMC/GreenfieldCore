package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class CodesModule extends Module {

    private Codes codes;
    private CodesConfig config;

    public CodesModule(GreenfieldCore plugin) {
        super(plugin);
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
