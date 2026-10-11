package net.greenfieldmc.core;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface IGreenfieldCorePlugin extends Plugin {

    @NotNull
    IServiceProvider getServiceProvider();

    @NotNull
    ModuleConfig getModuleConfig();

    void registerModule(@NotNull Module module);

}
