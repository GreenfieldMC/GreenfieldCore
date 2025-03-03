package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;

import java.util.function.Predicate;

public class CodesModule extends Module {

    private Codes codes;
    private CodesConfig config;

    public CodesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        config = new CodesConfig(plugin, this);
        codes = new Codes(config);

        new CodesCommand(plugin, this);
    }

    @Override
    public void tryDisable() {
        config.save();
    }

    public CodesConfig getConfig() {
        return config;
    }

    public Codes getCodes() {
        return codes;
    }

}
