package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.bcm.types.YmlConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenServerConfig extends YmlConfig {

    private final List<String> allowedCommands;
    private boolean enabled;

    public OpenServerConfig(Plugin plugin) {
        super(plugin, "openserver");

        addEntry("allowedCommands", Arrays.asList("/afk", "/tp", "/warp", "/warps"));
        addEntry("enabled", false);

        CommandMap map = null;
        try {
             Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
             field.setAccessible(true);
             map = (CommandMap) field.get(Bukkit.getServer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        this.enabled = getBoolean("enabled");
        this.allowedCommands = new ArrayList<>();
        CommandMap finalMap = map;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (finalMap == null) {
                plugin.getLogger().warning("CommandMap not found. OpenServer will not block commands!");
            } else {
                getStringList("allowedCommands").forEach(c -> {
                    Command command = finalMap.getCommand(c);
                    if (command == null) return;
                    allowedCommands.addAll(command.getAliases());
                    allowedCommands.add(command.getName());
                });
            }
        }, 1);

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

    @Override
    public void reload() {
        super.reload();
        allowedCommands.clear();
        allowedCommands.addAll(getStringList("allowedCommands"));
    }

    public void save() {
        setEntry("enabled", enabled);
    }

}
