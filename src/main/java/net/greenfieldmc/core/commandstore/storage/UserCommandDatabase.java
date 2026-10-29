package net.greenfieldmc.core.commandstore.storage;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

public class UserCommandDatabase extends AbstractCommandDatabase {
    
    public UserCommandDatabase(Plugin plugin, Logger logger, UUID player) {
        super(plugin, logger, "userCommands" + File.separator + player.toString());
    }
}
