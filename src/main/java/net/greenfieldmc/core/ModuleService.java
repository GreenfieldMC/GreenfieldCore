package net.greenfieldmc.core;

import org.bukkit.plugin.Plugin;

/**
 * Base class for a module service: a class registered in a {@link Module}'s service provider that has an
 * enable/disable lifecycle (loading config or storage, saving on shutdown, hooking an external plugin).
 * <p>
 * The constructor registers the service with the module, which then calls {@code tryEnable}, {@code postTryEnable}
 * and {@code tryDisable}. Obtain the {@link Module} parameter through DI; the module provider registers it.
 * <p>
 * Do not extend this for classes that need no lifecycle (commands, listeners, paginators, arguments, POJOs). Those
 * are plain classes. See {@link Module} for the full breakdown.
 *
 * @param <T> the service interface, which extends {@link IModuleService}
 */
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
