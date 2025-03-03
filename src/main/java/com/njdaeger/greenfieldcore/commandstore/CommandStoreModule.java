package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class CommandStoreModule extends Module {
    
    private Map<UUID, UserCommandStorage> userCommands;
    private ServerCommandStorage serverCommands;
    
    public CommandStoreModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }
    
    @Override
    public void tryEnable() {
        this.userCommands = new HashMap<>();
        this.serverCommands = new ServerCommandStorage(plugin);
        new CommandStoreCommands(plugin, this);
    }
    
    @Override
    public void tryDisable() {
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
