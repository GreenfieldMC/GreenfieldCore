package com.njdaeger.greenfieldcore;

//import com.earth2me.essentials.userstorage.IUserMap;

import com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule;
import com.njdaeger.greenfieldcore.authhub.AuthhubModule;
import com.njdaeger.greenfieldcore.chatformat.ChatFormatModule;
import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.commandstore.CommandStoreModule;
import com.njdaeger.greenfieldcore.hotspots.HotspotModule;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchModule;
import com.njdaeger.greenfieldcore.powershovel.PowerShovelModule;
import com.njdaeger.greenfieldcore.redblock.RedblockModule;
import com.njdaeger.greenfieldcore.testresult.TestResultModule;
import com.njdaeger.greenfieldcore.utilities.UtilitiesModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class GreenfieldCore extends JavaPlugin {

    private final ModuleConfig moduleConfig = new ModuleConfig(this);

    private final List<Module> MODULES = new ArrayList<>();

    @Override
    public void onEnable() {
        MODULES.addAll(List.of(
                new CoreModule(this, (c) -> true),
                new CodesModule(this, ModuleConfig::isCodesEnabled),
                new TestResultModule(this, ModuleConfig::isTestResultsEnabled),
                new PaintingSwitchModule(this, ModuleConfig::isPaintingSwitchEnabled),
                new UtilitiesModule(this, ModuleConfig::isUtilitiesEnabled),
                new AuthhubModule(this, ModuleConfig::isAuthHubEnabled),
                new CommandStoreModule(this, ModuleConfig::isCommandStoreEnabled),
                new HotspotModule(this, ModuleConfig::isHotspotsEnabled),
                new PowerShovelModule(this, ModuleConfig::isPowerShovelEnabled),
                new AdvancedBuildModule(this, ModuleConfig::isAdvancedBuildModeEnabled),
                new RedblockModule(this, ModuleConfig::isRedblockEnabled),
                new ChatFormatModule(this, ModuleConfig::isChatFormatEnabled)
        ));

        MODULES.forEach(Module::enable);
        Util.userNameMap.put(Util.CONSOLE_UUID, "Console");
        Util.getAllPlayers();
    }

    @Override
    public void onDisable() {
        MODULES.forEach(Module::disable);
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }
}
