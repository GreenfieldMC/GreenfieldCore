package com.njdaeger.greenfieldcore.commandstore;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class UserCommandStorage extends AbstractCommandStorage {
    
    public UserCommandStorage(Plugin plugin, UUID player) {
        super(plugin, "userCommands" + File.separator + player.toString());
    }
}
