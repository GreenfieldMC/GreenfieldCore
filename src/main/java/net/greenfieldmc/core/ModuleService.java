package net.greenfieldmc.core;

import org.bukkit.plugin.Plugin;

public abstract class ModuleService<T extends IModuleService<T>> implements IModuleService<T> {

    private final Plugin plugin;
    private final Module module;
    protected boolean isEnabled;

    public ModuleService(Plugin plugin, Module module) {
        this.plugin = plugin;
        this.module = module;
        this.isEnabled = false;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public abstract void tryEnable(Plugin plugin, Module module) throws Exception;

    @Override
    public void postTryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public abstract void tryDisable(Plugin plugin, Module module) throws Exception;

}
