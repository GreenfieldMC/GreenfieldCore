package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class TestResultConfig extends Configuration {

    public TestResultConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "testresults");

        addEntry("testing-groups", Collections.singletonList("Testing"));
        addEntry("passing-group", "Worker");
        addEntry("failing-group", "Spectator");
        addEntry("unwhitelist-on-fail", true);
        addEntry("kick-on-fail", true);
        addEntry("ban-on-fail", false);
    }

    public List<String> testingGroups() {
        return getStringList("testing-groups");
    }

    public String passingGroup() {
        return getString("passing-group");
    }

    public String failingGroup() {
        return getString("failing-group");
    }

    public boolean unwhitelistOnFail() {
        return getBoolean("unwhitelist-on-fail");
    }

    public boolean kickOnFail() {
        return getBoolean("kick-on-fail");
    }

    public boolean banOnFail() {
        return getBoolean("ban-on-fail");
    }

}
