package net.greenfieldmc.core;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;

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
 *     <li><b>Service</b> - a plain class registered as a singleton in {@link #buildModuleServices}. Use when other
 *     classes inject it. Its constructor does the "enable" work (load config, read storage, hook another plugin); if
 *     the constructor throws, the module fails to enable. Dependencies come in through the constructor, and a
 *     dependency that may be missing (Vault, CoreProtect, Dynmap...) is annotated {@code @Nullable}.</li>
 *     <li><b>Closeable service</b> - a service that also implements {@link AutoCloseable}. When the module disables,
 *     {@link #disable()} calls {@code close()} on every closeable singleton the module registered (save storage,
 *     unregister listeners...). Implementing it is optional; a service with nothing to do on shutdown can skip it.</li>
 *     <li><b>Neither</b> - a plain class that is not registered as a service. Use when nothing injects it and it has
 *     no shutdown work: command registrations, listeners, paginators, arguments, messages, POJOs. Build it in
 *     {@link #tryEnable} with {@code moduleServiceProvider.initialize(X.class)}. Instances created this way are
 *     never closed, so anything that needs shutdown work must be a registered singleton instead.</li>
 * </ul>
 * Services that depend on other plugins and are shared between modules (Vault, CoreProtect, WorldEdit, Essentials,
 * Dynmap) are registered at the plugin level in {@code GreenfieldCore}, not in a module.
 */
public abstract class Module {

    protected final IServiceProvider pluginServiceProvider;
    protected IServiceProvider moduleServiceProvider;
    protected final IGreenfieldCorePlugin plugin;
    protected boolean enabled = false;

    private final ModuleLogger moduleLogger;
    private final Predicate<ModuleConfig> canEnable;

    public Module(IGreenfieldCorePlugin plugin, Predicate<ModuleConfig> canEnable) {
        this.plugin = plugin;
        this.canEnable = canEnable;
        this.moduleLogger = new ModuleLogger(this);
        plugin.registerModule(this);
        this.pluginServiceProvider = plugin.getServiceProvider();
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
            tryEnable();
            this.enabled = true;
            getLogger().info("Module enabled.");
        } catch (Exception e) {
            this.enabled = false;
            getLogger().severe("Failed to enable module: " + e.getMessage());
        }
    }

    public final void disable() {
        if (!enabled || !canEnable.test(plugin.getModuleConfig())) return;
        try {
            tryDisable();
            closeSingletons();
            this.enabled = false;
            getLogger().info("Module disabled.");
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

}
