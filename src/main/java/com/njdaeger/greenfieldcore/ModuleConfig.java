package com.njdaeger.greenfieldcore;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.plugin.Plugin;

public class ModuleConfig extends Configuration {

    private final boolean advancedBuildMode;
    private final boolean chatFormat;
    private final boolean codes;
    private final boolean commandStore;
    private final boolean hotspots;
    private final boolean openServer;
    private final boolean paintingSwitch;
    private final boolean powerShovel;
    private final boolean redblock;
    private final boolean testResults;
    private final boolean utilities;
    private final boolean authHub;

    public ModuleConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "moduleConfig");

        //configuration to enable or disable modules
        addEntry("modules.advancedBuildMode", true);
        addEntry("modules.chatFormat", true);
        addEntry("modules.codes", true);
        addEntry("modules.commandStore", true);
        addEntry("modules.hotspots", true);
        addEntry("modules.openServer", true);
        addEntry("modules.paintingSwitch", true);
        addEntry("modules.powerShovel", true);
        addEntry("modules.redblock", true);
        addEntry("modules.testResults", true);
        addEntry("modules.utilities", true);
        addEntry("modules.authHub", true);

        save();

        advancedBuildMode = getBoolean("modules.advancedBuildMode");
        chatFormat = getBoolean("modules.chatFormat");
        codes = getBoolean("modules.codes");
        commandStore = getBoolean("modules.commandStore");
        hotspots = getBoolean("modules.hotspots");
        openServer = getBoolean("modules.openServer");
        paintingSwitch = getBoolean("modules.paintingSwitch");
        powerShovel = getBoolean("modules.powerShovel");
        redblock = getBoolean("modules.redblock");
        testResults = getBoolean("modules.testResults");
        utilities = getBoolean("modules.utilities");
        authHub = getBoolean("modules.authHub");

    }

    public boolean isAdvancedBuildModeEnabled() {
        return advancedBuildMode;
    }

    public boolean isChatFormatEnabled() {
        return chatFormat;
    }

    public boolean isCodesEnabled() {
        return codes;
    }

    public boolean isCommandStoreEnabled() {
        return commandStore;
    }

    public boolean isHotspotsEnabled() {
        return hotspots;
    }

    public boolean isOpenServerEnabled() {
        return openServer;
    }

    public boolean isPaintingSwitchEnabled() {
        return paintingSwitch;
    }

    public boolean isPowerShovelEnabled() {
        return powerShovel;
    }

    public boolean isRedblockEnabled() {
        return redblock;
    }

    public boolean isTestResultsEnabled() {
        return testResults;
    }

    public boolean isUtilitiesEnabled() {
        return utilities;
    }

    public boolean isAuthHubEnabled() {
        return authHub;
    }

}
