package net.greenfieldmc.core.commandstore.storage;

import net.greenfieldmc.core.commandstore.SavedCommand;

import java.util.List;

public interface ICommandDatabase {

    List<SavedCommand> getCommands();

    SavedCommand getCommand(int id);

    void saveCommand(SavedCommand command);

    void deleteCommand(int id);

    void saveDatabase();

    int getNextCommandId();

}
