package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.commandstore.SavedCommand;
import net.greenfieldmc.core.commandstore.storage.ICommandDatabase;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandDatabaseServiceImpl extends ModuleService<ICommandDatabaseService> implements ICommandDatabaseService {

    private final ICommandDatabase commandDatabase;

    public CommandDatabaseServiceImpl(Plugin plugin, Module module, ICommandDatabase commandDatabase) {
        super(plugin, module);
        this.commandDatabase = commandDatabase;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        commandDatabase.saveDatabase();
    }

    @Override
    public SavedCommand getCommand(int id) {
        return commandDatabase.getCommand(id);
    }

    @Override
    public List<SavedCommand> getCommands() {
        return commandDatabase.getCommands();
    }

    @Override
    public void addCommand(String command, String description) {
        var savedCommand = new SavedCommand(command, description, 0, commandDatabase.getNextCommandId());
        commandDatabase.saveCommand(savedCommand);
    }

    @Override
    public void deleteCommand(SavedCommand command) {
        commandDatabase.deleteCommand(command.getId());
    }

    @Override
    public void editCommand(SavedCommand savedCommand, @Nullable String command, @Nullable String description, boolean incrementUsage) {
        if (command != null) savedCommand.setCommand(command);
        if (description != null) savedCommand.setDescription(description);
        if (incrementUsage) savedCommand.incrementUsage();
        commandDatabase.saveCommand(savedCommand);
    }
}
