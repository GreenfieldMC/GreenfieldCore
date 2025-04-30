package net.greenfieldmc.core;

import org.bukkit.plugin.Plugin;

public interface IModuleService<T extends IModuleService<T>> {

    Plugin getPlugin();

    Module getModule();

    /**
     * Checks if this service is enabled. This will return null if called in tryEnable.
     * @return true if the service is enabled, false otherwise.
     */
    boolean isEnabled();

    void setEnabled(boolean enabled);

    void tryEnable(Plugin plugin, Module module) throws Exception;

    /**
     * This method is called after tryEnable has successfully ran and enabled the plugin. This is where you should do any setup that requires the module to be enabled.
     * @param plugin The plugin
     * @param module The module
     * @throws Exception If an error occurs while enabling the module.
     */
    void postTryEnable(Plugin plugin, Module module) throws Exception;

    void tryDisable(Plugin plugin, Module module) throws Exception;

}
