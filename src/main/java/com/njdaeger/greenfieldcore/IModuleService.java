package com.njdaeger.greenfieldcore;

import org.bukkit.plugin.Plugin;

public interface IModuleService<T extends IModuleService<T>> {

    Plugin getPlugin();

    Module getModule();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void tryEnable(Plugin plugin, Module module) throws Exception;

    void tryDisable(Plugin plugin, Module module) throws Exception;

}
