package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.commandstore.services.CommandStoreCommandService;
import com.njdaeger.greenfieldcore.commandstore.services.CommandStoreServiceImpl;
import com.njdaeger.greenfieldcore.commandstore.services.ICommandStoreService;

import java.util.function.Predicate;

public class CommandStoreModule extends Module {

    private ICommandStoreService commandStoreService;
    
    public CommandStoreModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }
    
    @Override
    public void tryEnable() {
        this.commandStoreService = enableIntegration(new CommandStoreServiceImpl(plugin, this), true);
        enableIntegration(new CommandStoreCommandService(plugin, this, commandStoreService), true);
    }
    
    @Override
    public void tryDisable() {
        disableIntegration(commandStoreService);
    }
}
