package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandStoreModule extends Module {
    
    private Map<UUID, UserCommandStorage> userCommands;
    private ServerCommandStorage serverCommands;
    
    public CommandStoreModule(GreenfieldCore plugin) {
        super(plugin);
    }
    
    @Override
    public void onEnable() {
        this.userCommands = new HashMap<>();
        this.serverCommands = new ServerCommandStorage(plugin);
        new CommandStoreCommands(plugin, this);
    }
    
    @Override
    public void onDisable() {
        userCommands.values().forEach(UserCommandStorage::save);
        serverCommands.save();
    }
    
    public ServerCommandStorage getServerStorage() {
        return serverCommands;
    }
    
    public UserCommandStorage getUserStorage(UUID user) {
        if (!userCommands.containsKey(user)) {
            userCommands.put(user, new UserCommandStorage(plugin, user));
        }
        return userCommands.get(user);
    }
}
