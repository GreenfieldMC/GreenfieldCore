package net.greenfieldmc.core;

import org.bukkit.plugin.Plugin;

/**
 * Marks a service interface as having a module-driven lifecycle. Service interfaces (e.g. {@code ICodesService})
 * extend this and their implementation extends {@link ModuleService}. Interfaces for services without a lifecycle
 * should not extend this. See {@link Module} for when to use a module service versus a plain class.
 */
public interface IModuleService<T extends IModuleService<T>> {

    Plugin getPlugin();

    /**
     * Checks if this service is enabled. This will return null if called in tryEnable.
     * @return true if the service is enabled, false otherwise.
     */
    boolean isEnabled();

    void setEnabled(boolean enabled);

    void tryEnable(IGreenfieldCorePlugin plugin, Module module) throws Exception;

    /**
     * This method is called after tryEnable has successfully ran and enabled the plugin. This is where you should do any setup that requires the module to be enabled.
     * @param plugin The plugin
     * @param module The module
     * @throws Exception If an error occurs while enabling the module.
     */
    void postTryEnable(IGreenfieldCorePlugin plugin, Module module) throws Exception;

    void tryDisable(IGreenfieldCorePlugin plugin, Module module) throws Exception;

}
