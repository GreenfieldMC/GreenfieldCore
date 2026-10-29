package net.greenfieldmc.core.commandstore.storage;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class ServerCommandDatabase extends AbstractCommandDatabase {
    
    public ServerCommandDatabase(Plugin plugin, Logger logger) {
        super(plugin, logger, "server-commands");
    }
}
