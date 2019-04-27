package com.njdaeger.greenfieldcore;

import com.njdaeger.bci.base.BCICommand;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.CommandStore;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.signs.SignEditorModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenfieldCore extends JavaPlugin {

    private final CommandStore commandStore = new CommandStore(this);
    private final OpenServerModule openServerModule = new OpenServerModule(this);
    private final SignEditorModule signEditorModule = new SignEditorModule(this);

    @Override
    public void onEnable() {
        openServerModule.onEnable();
        signEditorModule.onEnable();
    }

    @Override
    public void onDisable() {
        openServerModule.onDisable();
        signEditorModule.onDisable();
    }

    public void registerCommand(BCICommand<CommandContext, TabContext> command) {
        commandStore.registerCommand(command);
    }
}
