package net.greenfieldmc.core.shared.services;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Throws on construction if Vault (or a permission/chat provider) is unavailable, so it is only registered when Vault is present.
 */
public class VaultServiceImpl implements IVaultService {

    private final Permission permission;
    private final Chat chat;

    public VaultServiceImpl() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            throw new Exception("Vault not found");

        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionProvider == null)
            throw new Exception("No permission provider found");

        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (chatProvider == null)
            throw new Exception("No chat provider found");

        permission = permissionProvider.getProvider();
        chat = chatProvider.getProvider();
    }

    @Override
    public boolean isEnabled() {
        return permission.isEnabled();
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

    @Override
    public List<String> getGroupList() {
        if (!isEnabled()) return List.of();
        return List.of(permission.getGroups());
    }
}
