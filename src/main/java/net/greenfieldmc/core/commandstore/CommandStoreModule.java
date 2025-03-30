package net.greenfieldmc.core.commandstore;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.commandstore.services.CommandStoreCommandService;
import net.greenfieldmc.core.commandstore.services.CommandStoreServiceImpl;
import net.greenfieldmc.core.commandstore.services.ICommandStoreService;

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
