package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;

import java.util.function.Predicate;

public final class OpenServerModule extends Module {

    private OpenServerConfig config;

    public OpenServerModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
    }

    public boolean isCommandAllowed(String name) {
        return config.isCommandAllowed(name);
    }

    public void reload() {
        config.reload();
        config.save();
    }

    @Override
    public void tryEnable() {
        this.config = new OpenServerConfig(plugin);
        new OpenServerListener(plugin, this);
        new OpenServerCommand(plugin, this);
    }

    @Override
    public void tryDisable() {
        config.save();
    }
}
