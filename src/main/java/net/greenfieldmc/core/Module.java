package net.greenfieldmc.core;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * A Module is a feature of the plugin that can be toggled on or off in the config (see {@link ModuleConfig}). It is the
 * top-level unit: one per feature (Codes, TestResult, Redblock...), created once in {@code GreenfieldCore#onEnable}.
 * <p>
 * A module owns a child {@link IServiceProvider} (built in {@link #buildModuleServices}) which inherits everything from
 * the plugin-level provider. Anything the module needs is registered there.
 * <p>
 * Where a class belongs:
 * <ul>
 *     <li><b>Module</b> - extends {@code Module}. Represents the whole feature, owns the service provider, and decides
 *     which services are registered and what runs on enable/disable. Contains no feature logic itself.</li>
 *     <li><b>Module service</b> - extends {@link ModuleService} (implements {@link IModuleService}). Registered in
 *     {@link #buildModuleServices}. Use when the class holds state or logic that other classes inject AND it needs
 *     to be loaded on enable and/or saved on disable (config, storage, API/Vault hooks). The module drives its
 *     {@code tryEnable}/{@code tryDisable}, so a failing service logs a warning instead of killing the module.</li>
 *     <li><b>Neither</b> - a plain class with no module base class. Use when there is no enable/disable lifecycle:
 *     command registrations (built in {@link #tryEnable} with {@code moduleServiceProvider.initialize(X.class)},
 *     never registered as a service), listeners, paginators, arguments, messages, POJOs. Dependencies come in
 *     through the constructor.</li>
 * </ul>
 * Rule of thumb: needs enable/disable hooks -> module service; otherwise -> plain class. A module service must be
 * registered in {@link #buildModuleServices}, as its constructor registers it with the module.
 *
 * @see ModuleService
 */
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
            integrations.forEach(integration -> {
                try {
                    integration.tryDisable(plugin, this);
                    integration.setEnabled(false);
                } catch (Exception e) {
                    getLogger().warning("Integration " + integration.getClass().getSimpleName() + " not disabled... " + e.getMessage());
                }
            });
            closeSingletons();
            this.enabled = false;
        } catch (Exception e) {
            getLogger().severe("Failed to disable module: " + e.getMessage());
        }
    }

    /**
     * Closes every {@link AutoCloseable} singleton this module registered. Parent (plugin-level) singletons are copied
     * into the module provider as transients, so they are not included here.
     */
    private void closeSingletons() {
        if (moduleServiceProvider == null) return;
        moduleServiceProvider.getSingletons().values().stream()
                .filter(AutoCloseable.class::isInstance)
                .distinct()
                .forEach(singleton -> {
                    try {
                        ((AutoCloseable) singleton).close();
                    } catch (Exception e) {
                        getLogger().warning("Failed to close " + singleton.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                });
    }

    protected abstract IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) throws Exception;

    protected abstract void tryEnable() throws Exception;

    protected abstract void tryDisable() throws Exception;

    public void registerIntegration(IModuleService<?> integration) {
        integrations.add(integration);
    }

}
