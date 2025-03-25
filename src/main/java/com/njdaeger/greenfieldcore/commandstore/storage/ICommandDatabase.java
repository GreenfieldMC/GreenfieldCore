package com.njdaeger.greenfieldcore.commandstore.storage;

import com.njdaeger.greenfieldcore.commandstore.SavedCommand;

import java.util.List;

public interface ICommandDatabase {

    List<SavedCommand> getCommands();

    SavedCommand getCommand(int id);

    void saveCommand(SavedCommand command);

    void deleteCommand(int id);

    void saveDatabase();

    int getNextCommandId();

}
