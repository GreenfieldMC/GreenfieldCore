package com.njdaeger.greenfieldcore;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.userstorage.IUserMap;
import com.njdaeger.authenticationhub.ConnectionRequirement;
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
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class GreenfieldCore extends JavaPlugin {

    private CoreProtectAPI coreApi;
    private static GreenfieldCore instance;

    private final ModuleConfig moduleConfig = new ModuleConfig(this);

    private final OpenServerModule openServerModule = new OpenServerModule(this);
    private final CodesModule codesModule = new CodesModule(this);
    private final TestResultModule testResultModule = new TestResultModule(this);
    private final PaintingSwitchModule paintingSwitchModule = new PaintingSwitchModule(this);
    private final UtilitiesModule utilitiesModule = new UtilitiesModule(this);
    private final AuthHubIntegration authHubIntegration = new AuthHubIntegration(this);
    private final CommandStoreModule storeModule = new CommandStoreModule(this);
    private final HotspotModule hotspotModule = new HotspotModule(this);
    private final PowerShovelModule powerShovelModule = new PowerShovelModule(this);
    private final AdvancedBuildModule advancedBuildModule = new AdvancedBuildModule(this);
    private final RedblockModule redblockModule = new RedblockModule(this);
    private final ChatFormatModule chatFormatModule = new ChatFormatModule(this);

    @Override
    public void onLoad() {
        ProfileBanList pbl = Bukkit.getBanList(BanList.Type.PROFILE);
        new ConnectionRequirement("DISCORD_REQUIREMENT", (p) -> {
            if (p.hasPermission("greenfieldcore.discord.exempt")) {
                GreenfieldCore.logger().info("User " + p.getName() + " is exempted from having a linked discord profile.");
                return false;
            }
            else if (p.isWhitelisted() && !pbl.isBanned(p.getPlayerProfile())) {
                GreenfieldCore.logger().info("User " + p.getName() + " must have a linked discord profile.");
                return true;
            }
            GreenfieldCore.logger().info("User " + p.getName() + " does not need a linked discord profile - they were not found in the whitelist or they are a banned member.");
            return false;
        });

    }

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
            IUserMap userMap = Essentials.getPlugin(Essentials.class).getUsers();
            getLogger().info("Loading uuid to username map...");
            Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).map(userMap::loadUncachedUser).filter(Objects::nonNull).forEach(user -> Util.userNameMap.put(user.getUUID(), user.getLastAccountName()));
            getLogger().info("Loaded " + Util.userNameMap.size() + " uuid to username mappings.");
        });


        if (moduleConfig.isOpenServerEnabled()) openServerModule.onEnable();
        //signEditorModule.onEnable();
        if (moduleConfig.isCodesEnabled()) codesModule.onEnable();
        //treePlanterModule.onEnable();
        if (moduleConfig.isTestResultsEnabled()) testResultModule.onEnable();
        if (moduleConfig.isPaintingSwitchEnabled()) paintingSwitchModule.onEnable();
        if (moduleConfig.isUtilitiesEnabled()) utilitiesModule.onEnable();
        if (moduleConfig.isAuthHubEnabled()) authHubIntegration.onEnable();
        if (moduleConfig.isCommandStoreEnabled()) storeModule.onEnable();
        if (moduleConfig.isHotspotsEnabled()) hotspotModule.onEnable();
        if (moduleConfig.isPowerShovelEnabled()) powerShovelModule.onEnable();
        if (moduleConfig.isAdvancedBuildModeEnabled()) advancedBuildModule.onEnable();
        if (moduleConfig.isRedblockEnabled()) redblockModule.onEnable();
        if (moduleConfig.isChatFormatEnabled()) chatFormatModule.onEnable();
    }

    @Override
    public void onDisable() {
        if (moduleConfig.isTestResultsEnabled()) testResultModule.onDisable();
        if (moduleConfig.isOpenServerEnabled()) openServerModule.onDisable();
        if (moduleConfig.isCodesEnabled()) codesModule.onDisable();
        if (moduleConfig.isCommandStoreEnabled()) storeModule.onDisable();
        if (moduleConfig.isHotspotsEnabled()) hotspotModule.onDisable();
        if (moduleConfig.isPowerShovelEnabled()) powerShovelModule.onDisable();
        if (moduleConfig.isAdvancedBuildModeEnabled()) advancedBuildModule.onDisable();
        if (moduleConfig.isRedblockEnabled()) redblockModule.onDisable();
        if (moduleConfig.isChatFormatEnabled()) chatFormatModule.onDisable();
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
