package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.IModuleService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IVaultPermissionService extends IModuleService<IVaultPermissionService> {

    CompletableFuture<Boolean> addUserToGroup(String world, UUID uuid, String group);

    CompletableFuture<Boolean> removeUserFromGroup(String world, UUID uuid, String group);

}
