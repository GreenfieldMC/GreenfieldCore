package com.njdaeger.greenfieldcore.commandstore.services;

import com.njdaeger.greenfieldcore.IModuleService;

import java.util.UUID;

public interface ICommandStoreService extends IModuleService<ICommandStoreService> {

    ICommandDatabaseService getServerStorage();

    ICommandDatabaseService getUserStorage(UUID uuid);

}
