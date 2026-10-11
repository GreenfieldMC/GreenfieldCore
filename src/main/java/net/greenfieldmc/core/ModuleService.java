package net.greenfieldmc.core;

import org.bukkit.plugin.Plugin;

public abstract class ModuleService<T extends IModuleService<T>> implements IModuleService<T> {

    private final Plugin plugin;
    protected boolean isEnabled;

    public ModuleService(IGreenfieldCorePlugin plugin, Module module) {
        this.plugin = plugin;
        this.isEnabled = false;
        module.registerIntegration(this);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
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
    public abstract void tryEnable(IGreenfieldCorePlugin plugin, Module module) throws Exception;

    @Override
    public void postTryEnable(IGreenfieldCorePlugin plugin, Module module) throws Exception {

    }

    @Override
    public abstract void tryDisable(IGreenfieldCorePlugin plugin, Module module) throws Exception;

}
