package net.greenfieldmc.core.commandstore.storage;

import net.greenfieldmc.core.Module;
import org.bukkit.plugin.Plugin;

public class ServerCommandDatabase extends AbstractCommandDatabase {
    
    public ServerCommandDatabase(Plugin plugin, Module module) {
        super(plugin, module, "server-commands");
    }
}
