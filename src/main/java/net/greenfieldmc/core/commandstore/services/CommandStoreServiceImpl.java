package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.commandstore.storage.ServerCommandDatabase;
import net.greenfieldmc.core.commandstore.storage.UserCommandDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class CommandStoreServiceImpl implements ICommandStoreService, AutoCloseable {

    private final IGreenfieldCorePlugin plugin;
    private final Logger logger;
    private final CommandDatabaseServiceImpl serverStorage;
    private final Map<UUID, CommandDatabaseServiceImpl> userStorage = new HashMap<>();

    public CommandStoreServiceImpl(IGreenfieldCorePlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.serverStorage = new CommandDatabaseServiceImpl(new ServerCommandDatabase(plugin, logger));
    }

    @Override
    public void close() {
        serverStorage.close();
        userStorage.values().forEach(CommandDatabaseServiceImpl::close);
    }

    @Override
    public ICommandDatabaseService getServerStorage() {
        return this.serverStorage;
    }

    @Override
    public ICommandDatabaseService getUserStorage(UUID uuid) {
        return userStorage.computeIfAbsent(uuid, id -> new CommandDatabaseServiceImpl(new UserCommandDatabase(plugin, logger, id)));
    }
}
