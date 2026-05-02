package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;

public class GreenfieldApiListenerService extends ModuleService<GreenfieldApiListenerService> implements Listener {

    private final IGreenfieldUserApiService apiService;

    public GreenfieldApiListenerService(Plugin plugin, Module module, IGreenfieldUserApiService apiService) {
        super(plugin, module);
        this.apiService = apiService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (Exception e) {
            throw new Exception("Failed to enable GreenfieldApiListenerService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }


    /**
     * Attempts to find/create/update the user in the Greenfield system when they join the server.
     */
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        var foundUserTask = apiService.getUserByMinecraftUuid(e.getUniqueId());
        foundUserTask.thenApply(foundUserResult -> {
            if (foundUserResult.isFailure()) {
                getModule().getLogger().info("User " + e.getName() + " not found in Greenfield System. Creating new user...");
                var createUserTask = apiService.createUser(e.getUniqueId(), e.getName());
                var newUser = createUserTask.thenApply(createUserResult -> {
                    if (createUserResult.isFailure()) {
                        getModule().getLogger().severe("Failed to create user for " + e.getName() + " in Greenfield System! Error: " + createUserResult.getErrorMessage());
                        return null;
                    } else {
                        getModule().getLogger().info("Successfully created user for " + e.getName() + " in Greenfield System with ID " + createUserResult.getData().getUserId());
                        return createUserResult.getData();
                    }
                }).join();
                return newUser;
            } else {
                getModule().getLogger().info("User " + e.getName() + " found in Greenfield System with ID " + foundUserResult.getData().getUserId());
                var foundUser = foundUserResult.getData();
                if (foundUser.getUsername() == null || !foundUser.getUsername().equals(e.getName())) {
                    getModule().getLogger().info("Updating username for user " + e.getName() + " in Greenfield System to " + e.getName());
                    var updateUserTask = apiService.updateUser(foundUser.getMinecraftUuid(), e.getName());
                    var updatedUser = updateUserTask.thenApply(updateUserResult -> {
                        if (updateUserResult.isFailure()) {
                            getModule().getLogger().severe("Failed to update username for user " + e.getName() + " in Greenfield System! Error: " + updateUserResult.getErrorMessage());
                            return null;
                        } else {
                            getModule().getLogger().info("Successfully updated username for user " + e.getName() + " in Greenfield System to " + e.getName());
                            return updateUserResult.getData();
                        }
                    }).join();
                    return updatedUser;
                }
                return foundUserResult.getData();
            }
        });
    }
}
