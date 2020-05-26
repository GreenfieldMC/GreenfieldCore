package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import org.bukkit.Bukkit;

public final class OpenServerModule extends Module {

    private OpenServerConfig config;

    public OpenServerModule(GreenfieldCore plugin) {
        super(plugin);
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
    public void onEnable() {
        this.config = new OpenServerConfig(plugin);
        new OpenServerListener(plugin, this);
        new OpenServerCommand(plugin, this);
    }

    @Override
    public void onDisable() {
        config.save();
    }
}
