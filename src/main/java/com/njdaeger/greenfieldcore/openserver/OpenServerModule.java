package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class OpenServerModule implements Module {

    private boolean enabled;
    private OpenServerConfig config;
    private final GreenfieldCore plugin;

    public OpenServerModule(GreenfieldCore plugin) {
        this.plugin = plugin;
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

    public void addAllowedCommand(String name) {
        config.addAllowedCommand(name);
    }

    public void removeAllowedCommand(String name) {
        config.removeAllowedCommand(name);
    }

    public OpenServerConfig getConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        this.config = new OpenServerConfig(plugin);
        new OpenServerListener(plugin);
        new OpenServerCommand(plugin);
    }

    @Override
    public void onDisable() {
        config.save();
    }
}
