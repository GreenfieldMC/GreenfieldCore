package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.bcm.types.YmlConfig;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenServerConfig extends YmlConfig {

    private final List<String> allowedCommands = new ArrayList<>();
    private boolean enabled;

    public OpenServerConfig(Plugin plugin) {
        super(plugin, "openserver");

        addEntry("allowedCommands", Arrays.asList("/afk", "/tp", "/warp", "/warps"));
        addEntry("enabled", false);

        allowedCommands.addAll(getStringList("allowedCommands"));
        this.enabled = getBoolean("enabled");
    }

    public boolean isCommandAllowed(String name) {
        return allowedCommands.contains(name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reload() {
        allowedCommands.clear();
        allowedCommands.addAll(getStringList("allowedCommands"));
        this.enabled = getBoolean("enabled");
    }

    public void save() {
        setEntry("enabled", enabled);
    }

}
