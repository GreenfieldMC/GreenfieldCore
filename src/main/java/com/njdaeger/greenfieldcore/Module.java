package com.njdaeger.greenfieldcore;

import java.util.function.Predicate;

public abstract class Module {

    protected final GreenfieldCore plugin;
    protected boolean enabled = false;

    private final Predicate<ModuleConfig> canEnable;

    public Module(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        this.canEnable = canEnable;
        this.plugin = plugin;
    }

    public <T extends IModuleService<T>> T enableIntegration(IModuleService<T> integration, boolean required) {
        try {
            integration.tryEnable(plugin, this);
            integration.setEnabled(true);
            return (T) integration;
        } catch (Exception e) {
            if (required) throw new RuntimeException(e);
            return null;
        }
    }

    public <T extends IModuleService<T>> void disableIntegration(IModuleService<T> integration) {
        try {
            integration.tryDisable(plugin, this);
            integration.setEnabled(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(e);
        }
    }

    public final void disable() {
        if (!enabled || !canEnable.test(plugin.getModuleConfig())) return;
        try {
            tryDisable();
            this.enabled = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void tryEnable() throws Exception;

    protected abstract void tryDisable() throws Exception;

}
