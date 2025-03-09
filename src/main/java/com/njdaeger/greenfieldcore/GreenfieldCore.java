package com.njdaeger.greenfieldcore;

//import com.earth2me.essentials.userstorage.IUserMap;

import com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule;
import com.njdaeger.greenfieldcore.authhub.AuthHubIntegration;
import com.njdaeger.greenfieldcore.chatformat.ChatFormatModule;
import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.commandstore.CommandStoreModule;
import com.njdaeger.greenfieldcore.hotspots.HotspotModule;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchModule;
import com.njdaeger.greenfieldcore.powershovel.PowerShovelModule;
import com.njdaeger.greenfieldcore.redblock.RedblockModule;
import com.njdaeger.greenfieldcore.testresult.TestResultModule;
import com.njdaeger.greenfieldcore.utilities.UtilitiesModule;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class GreenfieldCore extends JavaPlugin {

    private CoreProtectAPI coreApi;
    private static GreenfieldCore instance;

    private final ModuleConfig moduleConfig = new ModuleConfig(this);

    private final List<Module> MODULES = new ArrayList<>();

    @Override
    public void onEnable() {
        GreenfieldCore.instance = this;
        coreApi = initializeCoreProtect();
        if (coreApi == null) {
            getLogger().warning("Unable to find an installation of CoreProtect. CoreProtect integration will be disabled.");
        } else {
            getLogger().info("CoreProtect integration enabled.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
//            IUserMap userMap = Essentials.getPlugin(Essentials.class).getUsers();
//            getLogger().info("Loading uuid to username map...");
//            Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).map(userMap::loadUncachedUser).filter(Objects::nonNull).forEach(user -> Util.userNameMap.put(user.getUUID(), user.getLastAccountName()));
//            getLogger().info("Loaded " + Util.userNameMap.size() + " uuid to username mappings.");
        });

        MODULES.addAll(List.of(
                new OpenServerModule(this, ModuleConfig::isOpenServerEnabled),
                new CodesModule(this, ModuleConfig::isCodesEnabled),
                new TestResultModule(this, ModuleConfig::isTestResultsEnabled),
                new PaintingSwitchModule(this, ModuleConfig::isPaintingSwitchEnabled),
                new UtilitiesModule(this, ModuleConfig::isUtilitiesEnabled),
                new AuthHubIntegration(this, ModuleConfig::isAuthHubEnabled),
                new CommandStoreModule(this, ModuleConfig::isCommandStoreEnabled),
                new HotspotModule(this, ModuleConfig::isHotspotsEnabled),
                new PowerShovelModule(this, ModuleConfig::isPowerShovelEnabled),
                new AdvancedBuildModule(this, ModuleConfig::isAdvancedBuildModeEnabled),
                new RedblockModule(this, ModuleConfig::isRedblockEnabled),
                new ChatFormatModule(this, ModuleConfig::isChatFormatEnabled)
        ));

        Util.userNameMap.put(Util.CONSOLE_UUID, "Console");
        Util.getAllPlayers();
        MODULES.forEach(Module::enable);
    }

    @Override
    public void onDisable() {
        MODULES.forEach(Module::disable);
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public CoreProtectAPI getCoreApi() {
        return coreApi;
    }

    public boolean isCoreProtectEnabled() {
        return coreApi != null;
    }

    private CoreProtectAPI initializeCoreProtect() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (!(plugin instanceof CoreProtect)) return null;
        CoreProtect cp = (CoreProtect)plugin;
        if (!cp.getAPI().isEnabled() || cp.getAPI().APIVersion() < 6) return null;
        else return cp.getAPI();
    }

    public static Logger logger() {
        return instance.getLogger();
    }

}
