package com.njdaeger.greenfieldcore.authhub.services;

import com.njdaeger.greenfieldcore.IModuleService;

public interface IAuthhubService extends IModuleService<IAuthhubService> {

    int getRequiredPatreonPledge();

}
