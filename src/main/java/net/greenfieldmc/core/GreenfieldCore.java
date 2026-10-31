package net.greenfieldmc.core;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.advancedbuild.AdvancedBuildModule;
import net.greenfieldmc.core.authhub.AuthhubModule;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import net.greenfieldmc.core.codes.CodesModule;
import net.greenfieldmc.core.commandstore.CommandStoreModule;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiClientImpl;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldApiListenerService;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldRedblockApiServiceImpl;
import net.greenfieldmc.core.greenfieldapi.services.GreenfieldUserApiServiceImpl;
import net.greenfieldmc.core.greenfieldapi.services.IAuthedClientService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldApiConfigService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldRedblockApiService;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;
import net.greenfieldmc.core.greenfieldapi.services.OAuthClientCredentialsHandler;
import net.greenfieldmc.core.hotspots.HotspotModule;
import net.greenfieldmc.core.paintingswitch.PaintingSwitchModule;
import net.greenfieldmc.core.powershovel.PowerShovelModule;
import net.greenfieldmc.core.redblock.RedblockModule;
import net.greenfieldmc.core.shared.services.DynmapServiceImpl;
import net.greenfieldmc.core.shared.services.EssentialsServiceImpl;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IVaultService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;
import net.greenfieldmc.core.shared.services.VaultServiceImpl;
import net.greenfieldmc.core.templates.TemplatesModule;
import net.greenfieldmc.core.testresult.TestResultModule;
import net.greenfieldmc.core.utilities.UtilitiesModule;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public final class GreenfieldCore extends JavaPlugin implements IGreenfieldCorePlugin {

    private IServiceProvider serviceProvider;
    private final ModuleConfig moduleConfig = new ModuleConfig(this);

    private final List<Module> MODULES = new ArrayList<>();

    @Override
    public void onEnable() {
        Util.userNameMap.put(Util.CONSOLE_UUID, "Console");
        Util.getAllPlayers();

        var builder = ServiceProviderBuilder.builder()
                .addTransient(Logger.class, (sp) -> getLogger())
                .addTransient(IGreenfieldCorePlugin.class, (sp) -> this)

                //greenfield api services
                .addSingleton(IGreenfieldApiConfigService.class, GreenfieldApiConfigService.class)
                .addSingleton(OAuthClientCredentialsHandler.class, (sp) -> {
                    var configService = sp.getService(IGreenfieldApiConfigService.class);
                    return new OAuthClientCredentialsHandler(configService.getApiUrl(),  configService.getClientId(), configService.getClientSecret(), getLogger());
                })
                .addTransient(IAuthedClientService.class, GreenfieldApiClientImpl.class)
                .addTransient(IGreenfieldUserApiService.class, GreenfieldUserApiServiceImpl.class)
                .addTransient(IGreenfieldRedblockApiService.class, GreenfieldRedblockApiServiceImpl.class)
                //end greenfield api services

                //standalone services - these depend on other plugins, so they are only registered if they can be created
                .addSingleton(IVaultService.class, (s) -> {
                    try {
                        return new VaultServiceImpl();
                    } catch (Exception e) {
                        getLogger().warning("VaultService not enabled... " + e.getMessage());
                        return null;
                    }
                })
                .addSingleton(ICoreProtectService.class, (s) -> {
                    try {
                        return new CoreProtectServiceImpl();
                    } catch (Exception e) {
                        getLogger().warning("CoreProtectService not enabled... " + e.getMessage());
                        return null;
                    }
                })
                .addSingleton(IWorldEditService.class, (s) -> {
                    try {
                        return new WorldEditServiceImpl();
                    } catch (Exception e) {
                        getLogger().warning("WorldEditService not enabled... " + e.getMessage());
                        return null;
                    }
                })
                .addSingleton(IDynmapService.class, (s) -> {
                    try {
                        return new DynmapServiceImpl(this, getLogger());
                    } catch (Exception e) {
                        getLogger().warning("DynmapService not enabled... " + e.getMessage());
                        return null;
                    }
                })
                .addSingleton(IEssentialsService.class, (s) -> {
                    try {
                        return new EssentialsServiceImpl();
                    } catch (Exception e) {
                        getLogger().warning("EssentialsService not enabled... " + e.getMessage());
                        return null;
                    }
                });
                //end standalone services

        this.serviceProvider = builder.build(this);

        serviceProvider.initialize(GreenfieldApiListenerService.class);
        serviceProvider.initialize(CodesModule.class);
        serviceProvider.initialize(TestResultModule.class);
        serviceProvider.initialize(PaintingSwitchModule.class);
        serviceProvider.initialize(UtilitiesModule.class);
        serviceProvider.initialize(CommandStoreModule.class);
        serviceProvider.initialize(HotspotModule.class);
        serviceProvider.initialize(PowerShovelModule.class);
        serviceProvider.initialize(AdvancedBuildModule.class);
        serviceProvider.initialize(RedblockModule.class);
        serviceProvider.initialize(ChatFormatModule.class);
        serviceProvider.initialize(TemplatesModule.class);
        serviceProvider.initialize(AuthhubModule.class);

        MODULES.forEach(Module::enable);
    }

    @Override
    public void onDisable() {
        MODULES.forEach(Module::disable);
    }

    @Override
    public @NotNull IServiceProvider getServiceProvider() {
        if (serviceProvider == null) throw new IllegalStateException("Service provider is not initialized yet.");
        return serviceProvider;
    }

    @Override
    public @NonNull ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    @Override
    public void registerModule(@NotNull Module module) {
        MODULES.add(module);
    }
}
