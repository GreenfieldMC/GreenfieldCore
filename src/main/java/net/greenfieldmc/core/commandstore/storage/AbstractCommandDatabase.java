package net.greenfieldmc.core.commandstore.storage;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.commandstore.SavedCommand;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractCommandDatabase extends Configuration implements ICommandDatabase {
    
    private final Map<Integer, SavedCommand> commands;
    private int commandIndex;
    
    public AbstractCommandDatabase(Plugin plugin, Module module, String configName) {
        super(plugin, ConfigType.YML, configName);
        this.commands =  new HashMap<>();
        this.commandIndex = 0;
        if (hasSection("commands")) {
            for (String key : getSection("commands").getKeys(false)) {
                var commandSection = getSection("commands." + key);
                var command = commandSection.getString("command");
                var description = commandSection.getString("description");
                var used = commandSection.getInt("used");
                int id;
                try {
                    id = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    module.getLogger().warning("Unable to load command " + key + ". Invalid command ID.");
                    continue; // Skip invalid keys
                }
                if (id >= commandIndex) commandIndex = id + 1;
                if (command == null || command.isBlank() || description == null || description.isBlank()) {
                    module.getLogger().warning("Unable to load command " + key + ". Command or description is empty.");
                    continue; // Skip empty commands
                }
                var savedCommand = new SavedCommand(command, description, used, id);
                commands.put(id, savedCommand);
            }
        }
    }

    @Override
    public List<SavedCommand> getCommands() {
        return commands.values().stream().toList();
    }

    @Override
    public SavedCommand getCommand(int id) {
        return commands.get(id);
    }

    @Override
    public void saveCommand(SavedCommand command) {
        commands.put(command.getId(), command);
        if (command.getId() >= commandIndex) commandIndex = command.getId() + 1;
        setEntry("commands." + command.getId() + ".command", command.getCommand());
        setEntry("commands." + command.getId() + ".description", command.getDescription());
        setEntry("commands." + command.getId() + ".used", command.getUsed());
        command.setChanged(false);
    }

    @Override
    public void deleteCommand(int id) {
        commands.remove(id);
        setEntry("commands." + id, null);
    }

    @Override
    public void saveDatabase() {
        commands.values().stream().filter(SavedCommand::hasChanged).forEach(sc -> {
            saveCommand(sc);
            sc.setChanged(false);
        });
        save();
    }

    @Override
    public int getNextCommandId() {
        return commandIndex;
    }

}
