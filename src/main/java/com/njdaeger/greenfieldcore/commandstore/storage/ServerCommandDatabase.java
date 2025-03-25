package com.njdaeger.greenfieldcore.commandstore.storage;

import com.njdaeger.greenfieldcore.Module;
import org.bukkit.plugin.Plugin;

public class ServerCommandDatabase extends AbstractCommandDatabase {
    
    public ServerCommandDatabase(Plugin plugin, Module module) {
        super(plugin, module, "server-commands");
    }
}
