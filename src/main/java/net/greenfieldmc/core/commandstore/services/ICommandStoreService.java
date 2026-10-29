package net.greenfieldmc.core.commandstore.services;

import java.util.UUID;

public interface ICommandStoreService {

    ICommandDatabaseService getServerStorage();

    ICommandDatabaseService getUserStorage(UUID uuid);

}
