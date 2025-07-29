package net.greenfieldmc.core;

import net.greenfieldmc.core.advancedbuild.AdvancedBuildModule;
import net.greenfieldmc.core.authhub.AuthhubModule;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import net.greenfieldmc.core.codes.CodesModule;
import net.greenfieldmc.core.commandstore.CommandStoreModule;
import net.greenfieldmc.core.hotspots.HotspotModule;
import net.greenfieldmc.core.paintingswitch.PaintingSwitchModule;
import net.greenfieldmc.core.powershovel.PowerShovelModule;
import net.greenfieldmc.core.redblock.RedblockModule;
import net.greenfieldmc.core.templates.TemplatesModule;
import net.greenfieldmc.core.testresult.TestResultModule;
import net.greenfieldmc.core.utilities.UtilitiesModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class GreenfieldCore extends JavaPlugin {

    private final ModuleConfig moduleConfig = new ModuleConfig(this);

    private final List<Module> MODULES = new ArrayList<>();

    @Override
    public void onEnable() {
        Util.userNameMap.put(Util.CONSOLE_UUID, "Console");
        Util.getAllPlayers();
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
                new ChatFormatModule(this, ModuleConfig::isChatFormatEnabled),
                new TemplatesModule(this, ModuleConfig::isTemplatesEnabled)
        ));

        MODULES.forEach(Module::enable);
    }

    @Override
    public void onDisable() {
        MODULES.forEach(Module::disable);
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }
}
