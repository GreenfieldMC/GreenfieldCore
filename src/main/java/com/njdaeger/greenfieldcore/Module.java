package com.njdaeger.greenfieldcore;

import java.util.function.Predicate;
import java.util.logging.Logger;

public abstract class Module {

    protected final GreenfieldCore plugin;
    protected boolean enabled = false;

    private final Logger moduleLogger;
    private final Predicate<ModuleConfig> canEnable;

    public Module(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        this.plugin = plugin;
        this.canEnable = canEnable;
        this.moduleLogger = new ModuleLogger(this);
    }

    public <T extends IModuleService<T>> T enableIntegration(IModuleService<T> integration, boolean required) {
        try {
            integration.tryEnable(plugin, this);
            integration.setEnabled(true);
            return (T) integration;
        } catch (Exception e) {
            if (required) throw new RuntimeException(e);
            getLogger().warning("Integration " + integration.getClass().getSimpleName() + " not enabled... " + e.getMessage());
            return (T) integration;
        }
    }

    public <T extends IModuleService<T>> void disableIntegration(IModuleService<T> integration) {
        if (!integration.isEnabled()) return;
        try {
            integration.tryDisable(plugin, this);
            integration.setEnabled(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Logger getLogger() {
        return moduleLogger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void enable() {
        if (enabled || !canEnable.test(plugin.getModuleConfig())) return;
        try {
            tryEnable();
            this.enabled = true;
        } catch (Exception e) {
            this.enabled = false;
            getLogger().severe("Failed to enable module: " + e.getMessage());
        }
    }

    public final void disable() {
        if (!enabled || !canEnable.test(plugin.getModuleConfig())) return;
        try {
            tryDisable();
            this.enabled = false;
        } catch (Exception e) {
            getLogger().severe("Failed to enable module: " + e.getMessage());
        }
    }

    protected abstract void tryEnable() throws Exception;

    protected abstract void tryDisable() throws Exception;

}
