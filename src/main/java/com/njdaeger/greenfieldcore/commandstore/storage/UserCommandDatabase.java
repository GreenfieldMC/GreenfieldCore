package com.njdaeger.greenfieldcore.commandstore.storage;

import com.njdaeger.greenfieldcore.Module;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class UserCommandDatabase extends AbstractCommandDatabase {
    
    public UserCommandDatabase(Plugin plugin, Module module, UUID player) {
        super(plugin, module, "userCommands" + File.separator + player.toString());
    }
}
