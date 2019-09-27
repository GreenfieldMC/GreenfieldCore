package com.njdaeger.greenfieldcore;

import com.njdaeger.bci.base.BCICommand;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.CommandStore;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.customtree.TreePlanterModule;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.signs.SignEditorModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenfieldCore extends JavaPlugin {

    private final CommandStore commandStore = new CommandStore(this);
    private final OpenServerModule openServerModule = new OpenServerModule(this);
    private final SignEditorModule signEditorModule = new SignEditorModule(this);
    private final CodesModule codesModule = new CodesModule(this);
    private final TreePlanterModule treePlanterModule = new TreePlanterModule(this);

    @Override
    public void onEnable() {
        openServerModule.onEnable();
        signEditorModule.onEnable();
        codesModule.onEnable();
        treePlanterModule.onEnable();
    }

    @Override
    public void onDisable() {
        openServerModule.onDisable();
        signEditorModule.onDisable();
        codesModule.onDisable();
    }

    public void registerCommand(BCICommand<CommandContext, TabContext> command) {
        commandStore.registerCommand(command);
    }
}
