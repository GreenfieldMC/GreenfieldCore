package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class GreenfieldApiListenerService implements Listener {

    private final IGreenfieldUserApiService apiService;
    private final IGreenfieldCorePlugin plugin;

    public GreenfieldApiListenerService(IGreenfieldCorePlugin plugin, IGreenfieldUserApiService apiService) {
        this.apiService = apiService;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Attempts to find/create/update the user in the Greenfield system when they join the server.
     */
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        var foundUserTask = apiService.getUserByMinecraftUuid(e.getUniqueId());
        foundUserTask.thenApply(foundUserResult -> {
            if (foundUserResult.isFailure()) {
                plugin.getLogger().info("User " + e.getName() + " not found in Greenfield System. Creating new user...");
                var createUserTask = apiService.createUser(e.getUniqueId(), e.getName());
                return createUserTask.thenApply(createUserResult -> {
                    if (createUserResult.isFailure()) {
                        plugin.getLogger().severe("Failed to create user for " + e.getName() + " in Greenfield System! Error: " + createUserResult.getErrorMessage());
                        return null;
                    } else {
                        plugin.getLogger().info("Successfully created user for " + e.getName() + " in Greenfield System with ID " + createUserResult.getData().getUserId());
                        return createUserResult.getData();
                    }
                }).join();
            } else {
                plugin.getLogger().info("User " + e.getName() + " found in Greenfield System with ID " + foundUserResult.getData().getUserId());
                var foundUser = foundUserResult.getData();
                if (foundUser.getUsername() == null || !foundUser.getUsername().equals(e.getName())) {
                    plugin.getLogger().info("Updating username for user " + e.getName() + " in Greenfield System to " + e.getName());
                    var updateUserTask = apiService.updateUser(foundUser.getMinecraftUuid(), e.getName());
                    return updateUserTask.thenApply(updateUserResult -> {
                        if (updateUserResult.isFailure()) {
                            plugin.getLogger().severe("Failed to update username for user " + e.getName() + " in Greenfield System! Error: " + updateUserResult.getErrorMessage());
                            return null;
                        } else {
                            plugin.getLogger().info("Successfully updated username for user " + e.getName() + " in Greenfield System to " + e.getName());
                            return updateUserResult.getData();
                        }
                    }).join();
                }
                return foundUserResult.getData();
            }
        });
    }
}
