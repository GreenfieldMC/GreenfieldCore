package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.commandstore.storage.ServerCommandDatabase;
import net.greenfieldmc.core.commandstore.storage.UserCommandDatabase;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandStoreServiceImpl extends ModuleService<ICommandStoreService> implements ICommandStoreService {

    private ICommandDatabaseService serverStorage;
    private final Map<UUID, ICommandDatabaseService> userStorage = new HashMap<>();

    public CommandStoreServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        this.serverStorage = module.enableIntegration(new CommandDatabaseServiceImpl(plugin, module, new ServerCommandDatabase(plugin, module)), true);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        module.disableIntegration(serverStorage);
        userStorage.forEach((uuid, storage) -> module.disableIntegration(storage));
    }

    @Override
    public ICommandDatabaseService getServerStorage() {
        return this.serverStorage;
    }

    @Override
    public ICommandDatabaseService getUserStorage(UUID uuid) {
        if (!userStorage.containsKey(uuid)) {
            userStorage.put(uuid, getModule().enableIntegration(new CommandDatabaseServiceImpl(getPlugin(), getModule(), new UserCommandDatabase(getPlugin(), getModule(), uuid)), true));
        }
        return userStorage.get(uuid);
    }
}
