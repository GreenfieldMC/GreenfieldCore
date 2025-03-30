package net.greenfieldmc.core.commandstore.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.greenfieldmc.core.commandstore.SavedCommand;
import net.greenfieldmc.core.commandstore.services.ICommandDatabaseService;
import net.greenfieldmc.core.commandstore.services.ICommandStoreService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractIntegerTypedArgument;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.stream.Collectors;

public class CommandIdArgument extends AbstractIntegerTypedArgument<SavedCommand> {

    private static final DynamicCommandExceptionType NO_COMMANDS = new DynamicCommandExceptionType(o -> () -> "No saved commands found for " + o.toString());
    private static final DynamicCommandExceptionType COMMAND_NOT_FOUND_SERVER = new DynamicCommandExceptionType(o -> () -> "Saved server command " + o.toString() + " not found");
    private static final DynamicCommandExceptionType COMMAND_NOT_FOUND_USER = new DynamicCommandExceptionType(o -> () -> "Saved user command " + o.toString() + " not found");

    private final ICommandStoreService commandStorageService;
    private final boolean noSuggestions;
    private final boolean serverCommands;

    public CommandIdArgument(ICommandStoreService commandStorageService, boolean noSuggestions, boolean serverCommands) {
        super(0, Integer.MAX_VALUE);
        this.commandStorageService = commandStorageService;
        this.noSuggestions = noSuggestions;
        this.serverCommands = serverCommands;
    }

    @Override
    public Map<SavedCommand, Message> listSuggestions(ICommandContext commandContext) {
        if (noSuggestions) return Map.of();
        var storage = resolveDatabase(commandContext);
        return storage.getCommands().stream().collect(Collectors.toMap(sc -> sc, sc -> sc::getDescription));
    }

    @Override
    public Integer convertToNative(SavedCommand savedCommand) {
        return savedCommand.getId();
    }

    @Override
    public SavedCommand convertToCustom(CommandSender sender, Integer nativeType, StringReader reader) throws CommandSyntaxException {
        if (serverCommands) {
            var serverStorage = commandStorageService.getServerStorage();
            var command = serverStorage.getCommand(nativeType);
            if (command != null) return command;
            reader.setCursor(reader.getCursor() - String.valueOf(nativeType).length());
            throw COMMAND_NOT_FOUND_SERVER.createWithContext(reader, nativeType);
        }
        if (sender instanceof Player player) {
            var userStorage = commandStorageService.getUserStorage(player.getUniqueId());
            var command = userStorage.getCommand(nativeType);
            if (command != null) return command;
            reader.setCursor(reader.getCursor() - String.valueOf(nativeType).length());
            throw COMMAND_NOT_FOUND_USER.createWithContext(reader, nativeType);
        }
        throw NO_COMMANDS.createWithContext(reader, sender == null ? "null" : sender.getName());
    }

    private ICommandDatabaseService resolveDatabase(ICommandContext ctx) {
        var server = ctx.argAtOrDefault(0, "").equalsIgnoreCase("server");
        if (server || ctx.isConsole()) return commandStorageService.getServerStorage();
        try {
            return commandStorageService.getUserStorage(ctx.asPlayer().getUniqueId());
        } catch (CommandSenderTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
