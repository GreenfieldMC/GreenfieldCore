package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.IModuleService;

import java.util.UUID;

public interface ICommandStoreService extends IModuleService<ICommandStoreService> {

    ICommandDatabaseService getServerStorage();

    ICommandDatabaseService getUserStorage(UUID uuid);

}
