package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import com.njdaeger.pdk.config.ISection;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommandStorage extends Configuration {
    
    private final Map<Integer, Command> commands;
    private int currentLastId;
    
    public AbstractCommandStorage(Plugin plugin, String configName) {
        super(plugin, ConfigType.YML, configName);
        this.commands =  new HashMap<>();
        this.currentLastId = -1;
        if (hasSection("commands")) {
            for (String key : getSection("commands").getKeys(false)) {
                ISection cmd = getSection("commands." + key);
                String command = cmd.getString("command");
                String description = cmd.getString("description");
                int used = cmd.getInt("used");
                int id;
                try {
                    id = Integer.parseInt(cmd.getName());
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Could not load server command " + cmd.getName() + ". Invalid command ID. Must be numeric, must be unique.");
                    continue;
                }
                
                if (currentLastId < id) currentLastId = id;
                
                if (command == null || description == null) {
                    plugin.getLogger().warning("Could not load server command " + cmd.getName());
                    continue;
                }
                
                commands.put(id, new Command(command, description, used, id));
            }
        }
    }
    
    public Collection<Command> getCommands() {
        return commands.values();
    }
    
    public Command getCommand(int id) {
        return commands.get(id);
    }
    
    public void addCommand(String command, String description) {
        commands.put(++currentLastId, new Command(command, description, 0, currentLastId));
    }
    
    public void removeCommand(int id) {
        Command command = commands.remove(id);
        setEntry("commands." + command.getId() + ".command", null);
        setEntry("commands." + command.getId() + ".description", null);
        setEntry("commands." + command.getId() + ".used", null);
        setEntry("commands." + command.getId(), null);
    }
    
    public void save() {
        for (Command command : commands.values()) {
            setEntry("commands." + command.getId() + ".command", command.getCommand());
            setEntry("commands." + command.getId() + ".description", command.getDescription());
            setEntry("commands." + command.getId() + ".used", command.getUsed());
        }
        super.save();
    }
    
    public class Command implements PageItem<CommandContext> {
        
        private String command;
        private String description;
        private int used;
        private final int id;
        
        public Command(String command, String description, int used, int id) {
            this.command = command;
            this.description = description;
            this.used = used;
            this.id = id;
        }
    
        public String getCommand() {
            return command;
        }
    
        public void setCommand(String command) {
            this.command = command;
        }
    
        public String getDescription() {
            return description;
        }
    
        public void setDescription(String description) {
            this.description = description;
        }
        
        public int getUsed() {
            return used;
        }
        
        public void incrementUsage() {
            used++;
        }
        
        public int getId() {
            return id;
        }

        @Override
        public String getPlainItemText(ChatPaginator<?, CommandContext> paginator, CommandContext generatorInfo) {
            return description;
        }
    }
    
}
