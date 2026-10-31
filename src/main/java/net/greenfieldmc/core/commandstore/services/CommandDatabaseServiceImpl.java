package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.commandstore.SavedCommand;
import net.greenfieldmc.core.commandstore.storage.ICommandDatabase;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.List;

public class CommandDatabaseServiceImpl implements ICommandDatabaseService, AutoCloseable {

    private final ICommandDatabase commandDatabase;

    public CommandDatabaseServiceImpl(ICommandDatabase commandDatabase) {
        this.commandDatabase = commandDatabase;
    }

    @Override
    public void close() {
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
