package com.njdaeger.greenfieldcore.commandstore;

import org.bukkit.plugin.Plugin;

public class ServerCommandStorage extends AbstractCommandStorage {
    
    public ServerCommandStorage(Plugin plugin) {
        super(plugin, "server-commands");
    }
}
