package net.greenfieldmc.core;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public abstract class Module {

    protected final IServiceProvider pluginServiceProvider;
    protected IServiceProvider moduleServiceProvider;
    protected final IGreenfieldCorePlugin plugin;
    protected boolean enabled = false;

    private final ModuleLogger moduleLogger;
    private final Predicate<ModuleConfig> canEnable;
    private final List<IModuleService<?>> integrations = new ArrayList<>();

    public Module(IGreenfieldCorePlugin plugin, Predicate<ModuleConfig> canEnable) {
        this.plugin = plugin;
        this.canEnable = canEnable;
        this.moduleLogger = new ModuleLogger(this);
        plugin.registerModule(this);
        this.pluginServiceProvider = plugin.getServiceProvider();
    }

    public <T extends IModuleService<T>> T enableIntegration(IModuleService<T> integration, boolean required) {
        try {
            integration.tryEnable(plugin, this);
            integration.setEnabled(true);
            integration.postTryEnable(plugin, this);
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
            this.moduleServiceProvider = buildModuleServices(ServiceProviderBuilder.builder(moduleLogger.getPrefix(), pluginServiceProvider)
                    .addSingleton(Logger.class, (s) -> moduleLogger)
                    .addSingleton(Module.class, (s) -> this));
            integrations.forEach(integration -> {
                try {
                    integration.tryEnable(plugin, this);
                    integration.setEnabled(true);
                    integration.postTryEnable(plugin, this);
                } catch (Exception e) {
                    getLogger().warning("Integration " + integration.getClass().getSimpleName() + " not enabled... " + e.getMessage());
                }
            });
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
            integrations.forEach(integration -> {
                try {
                    integration.tryDisable(plugin, this);
                    integration.setEnabled(false);
                } catch (Exception e) {
                    getLogger().warning("Integration " + integration.getClass().getSimpleName() + " not disabled... " + e.getMessage());
                }
            });
            this.enabled = false;
        } catch (Exception e) {
            getLogger().severe("Failed to disable module: " + e.getMessage());
        }
    }

    protected abstract IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) throws Exception;

    protected abstract void tryEnable() throws Exception;

    protected abstract void tryDisable() throws Exception;

    public void registerIntegration(IModuleService<?> integration) {
        integrations.add(integration);
    }

}
