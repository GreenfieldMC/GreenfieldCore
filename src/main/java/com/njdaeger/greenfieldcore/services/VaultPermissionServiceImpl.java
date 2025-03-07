package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.Util;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultPermissionServiceImpl extends ModuleService<IVaultPermissionService> implements IVaultPermissionService {

    private Permission permission;

    public VaultPermissionServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            throw new Exception("Vault not found for VaultServiceImpl");

        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionProvider == null)
            throw new Exception("No permission provider found for VaultServiceImpl");

        permission = permissionProvider.getProvider();
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && permission != null && permission.isEnabled();
    }

    @Override
    public CompletableFuture<Boolean> addUserToGroup(String world, UUID uuid, String group) {
        if (!isEnabled()) {
            return CompletableFuture.supplyAsync(() -> false);
        }
        return CompletableFuture.supplyAsync(() -> permission.playerAddGroup(world, Bukkit.getOfflinePlayer(uuid), group));
    }

    @Override
    public CompletableFuture<Boolean> removeUserFromGroup(String world, UUID uuid, String group) {
        if (!isEnabled()) {
            return CompletableFuture.supplyAsync(() -> false);
        }
        return CompletableFuture.supplyAsync(() -> permission.playerRemoveGroup(world, Bukkit.getOfflinePlayer(uuid), group));
    }
}
