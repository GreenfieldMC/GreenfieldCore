package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IVaultService extends IModuleService<IVaultService> {

    CompletableFuture<Boolean> addUserToGroup(String world, UUID uuid, String group);

    CompletableFuture<Boolean> removeUserFromGroup(String world, UUID uuid, String group);

}
