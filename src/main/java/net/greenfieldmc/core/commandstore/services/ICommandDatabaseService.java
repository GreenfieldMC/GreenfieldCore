package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.commandstore.SavedCommand;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public interface ICommandDatabaseService extends IModuleService<ICommandDatabaseService> {

    SavedCommand getCommand(int id);

    List<SavedCommand> getCommands();

    default List<SavedCommand> getCommandsBy(CommandSearchMode mode, @Nullable String... query) {
        if ((query == null || query.length == 0 || query[0] == null) && (mode == CommandSearchMode.COMMAND || mode == CommandSearchMode.DESCRIPTION))
            throw new IllegalArgumentException("Query cannot be null or empty when searching by COMMAND or DESCRIPTION");
        return switch (mode) {
            case FREQUENCY -> getCommands().stream().sorted(Comparator.comparingInt(SavedCommand::getUsed)).toList();
            case ID -> getCommands().stream().sorted(Comparator.comparingInt(SavedCommand::getId)).toList();
            case COMMAND -> getCommands().stream().sorted(Comparator.comparingInt(cmd -> {
                var dist = StringUtils.getLevenshteinDistance(cmd.getCommand(), query[0]);
                var index = cmd.getCommand().indexOf(query[0]);
                return Math.min(dist, index);
            })).toList();
            case DESCRIPTION -> getCommands().stream().sorted(Comparator.comparingInt(cmd -> {
                var dist = StringUtils.getLevenshteinDistance(cmd.getDescription(), query[0]);
                var index = cmd.getDescription().indexOf(query[0]);
                return Math.min(dist, index);
            })).toList();
        };
    }

    void addCommand(String command, String description);

    void deleteCommand(SavedCommand command);

    void editCommand(SavedCommand savedCommand, @Nullable String command, @Nullable String description, boolean incrementUsage);

    enum CommandSearchMode {
        FREQUENCY,
        ID,
        COMMAND,
        DESCRIPTION
    }

}
