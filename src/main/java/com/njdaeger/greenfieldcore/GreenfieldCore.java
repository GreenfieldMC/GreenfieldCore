package com.njdaeger.greenfieldcore;

import com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule;
import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.commandstore.CommandStoreModule;
import com.njdaeger.greenfieldcore.hotspots.HotspotModule;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchModule;
import com.njdaeger.greenfieldcore.powershovel.PowerShovelModule;
import com.njdaeger.greenfieldcore.testresult.TestResultModule;
import com.njdaeger.greenfieldcore.utilities.UtilitiesModule;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenfieldCore extends JavaPlugin {

    private CoreProtectAPI coreApi;

    private final OpenServerModule openServerModule = new OpenServerModule(this);
    private final CodesModule codesModule = new CodesModule(this);
    private final TestResultModule testResultModule = new TestResultModule(this);
    private final PaintingSwitchModule paintingSwitchModule = new PaintingSwitchModule(this);
    private final UtilitiesModule utilitiesModule = new UtilitiesModule(this);
    private final MCLinkIntegration mcLinkModule = new MCLinkIntegration(this);
    private final CommandStoreModule storeModule = new CommandStoreModule(this);
    private final HotspotModule hotspotModule = new HotspotModule(this);
    private final PowerShovelModule powerShovelModule = new PowerShovelModule(this);
    private final AdvancedBuildModule advancedBuildModule = new AdvancedBuildModule(this);

    @Override
    public void onEnable() {
        coreApi = initializeCoreProtect();
        if (coreApi == null) {
            getLogger().warning("Unable to find an installation of CoreProtect. CoreProtect integration will be disabled.");
        } else {
            getLogger().info("CoreProtect integration enabled.");
        }

        openServerModule.onEnable();
        //signEditorModule.onEnable();
        codesModule.onEnable();
        //treePlanterModule.onEnable();
        testResultModule.onEnable();
        paintingSwitchModule.onEnable();
        utilitiesModule.onEnable();
        mcLinkModule.onEnable();
        storeModule.onEnable();
        hotspotModule.onEnable();
        powerShovelModule.onEnable();
        advancedBuildModule.onEnable();
    }

    @Override
    public void onDisable() {
        testResultModule.onDisable();
        openServerModule.onDisable();
        codesModule.onDisable();
        storeModule.onDisable();
        hotspotModule.onDisable();
        powerShovelModule.onDisable();
        advancedBuildModule.onDisable();
    }

    public CoreProtectAPI getCoreApi() {
        return coreApi;
    }

    private CoreProtectAPI initializeCoreProtect() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (!(plugin instanceof CoreProtect)) return null;
        CoreProtect cp = (CoreProtect)plugin;
        if (!cp.getAPI().isEnabled() || cp.getAPI().APIVersion() < 6) return null;
        else return cp.getAPI();
    }

}
