package net.greenfieldmc.core.commandstore.storage;

import net.greenfieldmc.core.Module;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class UserCommandDatabase extends AbstractCommandDatabase {
    
    public UserCommandDatabase(Plugin plugin, Module module, UUID player) {
        super(plugin, module, "userCommands" + File.separator + player.toString());
    }
}
