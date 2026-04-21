package net.greenfieldmc.core;

import net.greenfieldmc.core.advancedbuild.AdvancedBuildModule;
import net.greenfieldmc.core.authhub.AuthhubModule;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import net.greenfieldmc.core.codes.CodesModule;
import net.greenfieldmc.core.commandstore.CommandStoreModule;
import net.greenfieldmc.core.greenfieldapi.GreenfieldApiModule;
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

        var coreModule = new CoreModule(this, (c) -> true);
        var codesModule = new CodesModule(this, ModuleConfig::isCodesEnabled);
        var testResultModule = new TestResultModule(this, ModuleConfig::isTestResultsEnabled);
        var paintingSwitchModule = new PaintingSwitchModule(this, ModuleConfig::isPaintingSwitchEnabled);
        var utilitiesModule = new UtilitiesModule(this, ModuleConfig::isUtilitiesEnabled);
        var commandStoreModule = new CommandStoreModule(this, ModuleConfig::isCommandStoreEnabled);
        var hotspotModule = new HotspotModule(this, ModuleConfig::isHotspotsEnabled);
        var powerShovelModule = new PowerShovelModule(this, ModuleConfig::isPowerShovelEnabled);
        var advancedBuildModule = new AdvancedBuildModule(this, ModuleConfig::isAdvancedBuildModeEnabled);
        var redblockModule = new RedblockModule(this, ModuleConfig::isRedblockEnabled);
        var chatFormatModule = new ChatFormatModule(this, ModuleConfig::isChatFormatEnabled);
        var templatesModule = new TemplatesModule(this, ModuleConfig::isTemplatesEnabled);
        var greenfieldApiModule = new GreenfieldApiModule(this, ModuleConfig::isGreenfieldApiEnabled);
        var authhubModule = new AuthhubModule(this, ModuleConfig::isAuthHubEnabled, greenfieldApiModule);

        MODULES.addAll(List.of(
                coreModule,
                codesModule,
                testResultModule,
                paintingSwitchModule,
                utilitiesModule,
                commandStoreModule,
                hotspotModule,
                powerShovelModule,
                advancedBuildModule,
                redblockModule,
                chatFormatModule,
                templatesModule,
                greenfieldApiModule,
                authhubModule
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
