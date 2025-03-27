package com.njdaeger.greenfieldcore;

import com.njdaeger.greenfieldcore.shared.services.EssentialsServiceImpl;

import java.util.function.Predicate;

public class CoreModule extends Module {

    public CoreModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        enableIntegration(new EssentialsServiceImpl(plugin, this), false).loadUsernameMap();
    }

    @Override
    protected void tryDisable() throws Exception {

    }
}
