package net.greenfieldmc.core.authhub.services;

import net.greenfieldmc.core.IModuleService;

public interface IAuthhubService extends IModuleService<IAuthhubService> {

    int getRequiredPatreonPledge();

}
