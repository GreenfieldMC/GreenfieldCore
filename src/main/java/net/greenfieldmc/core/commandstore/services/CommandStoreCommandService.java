package net.greenfieldmc.core.commandstore.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.commandstore.CommandStoreMessages;
import net.greenfieldmc.core.commandstore.SavedCommand;
import net.greenfieldmc.core.commandstore.arguments.CommandIdArgument;
import net.greenfieldmc.core.commandstore.arguments.CommandArgument;
import net.greenfieldmc.core.commandstore.arguments.EditCommandArgument;
import net.greenfieldmc.core.commandstore.arguments.EditDescriptionArgument;
import net.greenfieldmc.core.commandstore.paginators.CommandPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CommandStoreCommandService extends ModuleService<CommandStoreCommandService> implements IModuleService<CommandStoreCommandService> {

    private static final ChatPaginator<SavedCommand, ICommandContext> LIST_PAGINATOR = new CommandPaginator(CommandPaginator.CommandPaginatorMode.LIST).build();
    private static final ChatPaginator<SavedCommand, ICommandContext> QUERY_PAGINATOR = new CommandPaginator(CommandPaginator.CommandPaginatorMode.QUERY).build();

    private final ICommandStoreService commandStorageService;

    public CommandStoreCommandService(Plugin plugin, Module module, ICommandStoreService commandStorageService) {
        super(plugin, module);
        this.commandStorageService = commandStorageService;
    }

    private void addCommand(ICommandContext ctx) throws PDKCommandException {
        var confirm = ctx.hasFlag("confirm");
        var flagMap = ctx.getFlags();
        flagMap.entrySet().forEach(flg -> System.out.println(flg.getKey() + " : " + flg.getValue()));

        var storage = resolveDatabase(ctx);
        var description = ctx.getTyped("description", String.class);
        var command = ctx.getTyped("command", String.class);

        if (confirm) {
            storage.addCommand(command, description);
            ctx.send(CommandStoreMessages.ADD_COMMAND_SUCCESS);
        } else {
            ctx.send(CommandStoreMessages.ADD_COMMAND_CONFIRM);
            if (ctx.isConsole()) ctx.send(CommandStoreMessages.ADD_COMMAND_CONFIRM_CONSOLE);
            else ctx.send(CommandStoreMessages.ADD_COMMAND_CONFIRM_PLAYER.apply(ctx));
        }
    }

    private void removeCommand(ICommandContext ctx) throws PDKCommandException {
        var confirm = ctx.hasFlag("confirm");

        var storage = resolveDatabase(ctx);
        var command = ctx.getTyped("commandId", SavedCommand.class);

        if (confirm) {
            storage.deleteCommand(command);
            ctx.send(CommandStoreMessages.REMOVE_COMMAND_SUCCESS);
        } else {
            ctx.send(CommandStoreMessages.REMOVE_COMMAND_CONFIRM);
            if (ctx.isConsole()) ctx.send(CommandStoreMessages.REMOVE_COMMAND_CONFIRM_CONSOLE);
            else ctx.send(CommandStoreMessages.REMOVE_COMMAND_CONFIRM_PLAYER.apply(ctx));
        }
    }

    private void editCommand(ICommandContext ctx) throws PDKCommandException {
        var confirm = ctx.hasFlag("confirm");

        var storage = resolveDatabase(ctx);
        var command = ctx.getTyped("commandId", SavedCommand.class);

        var descriptionEdit = ctx.argAt(1).equalsIgnoreCase("description");
        var newValue = descriptionEdit ? ctx.getTyped("description", String.class) : ctx.getTyped("command", String.class);

        if (confirm) {
            if (descriptionEdit) storage.editCommand(command, null, newValue, false);
            else storage.editCommand(command, newValue, null, false);
            ctx.send(CommandStoreMessages.EDIT_COMMAND_SUCCESS);
        } else {
            ctx.send(CommandStoreMessages.EDIT_COMMAND_CONFIRM);
            if (ctx.isConsole()) ctx.send(CommandStoreMessages.EDIT_COMMAND_CONFIRM_CONSOLE);
            else ctx.send(CommandStoreMessages.EDIT_COMMAND_CONFIRM_PLAYER.apply(ctx));
        }
    }

    private void listCommands(ICommandContext ctx) throws PDKCommandException {
        var storage = resolveDatabase(ctx);
        var frequency = ctx.hasFlag("frequency");
        var page = ctx.getFlag("page", 1);

        var commands = frequency
                ? storage.getCommandsBy(ICommandDatabaseService.CommandSearchMode.FREQUENCY)
                : storage.getCommandsBy(ICommandDatabaseService.CommandSearchMode.ID);
        LIST_PAGINATOR.generatePage(ctx, commands, page).sendTo(CommandStoreMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void wrapCommand(ICommandContext ctx) throws PDKCommandException {
        var storage = resolveDatabase(ctx);
        var command = ctx.getTyped("commandId", SavedCommand.class);

        try {
            ctx.getSender().getServer().dispatchCommand(ctx.getSender(), command.getCommand().substring(1));
            Bukkit.getLogger().info(ctx.getSender().getName() + " issued server command: " + command.getCommand());
            storage.editCommand(command, null, null, true);
        } catch (Exception e) {
            ctx.error(CommandStoreMessages.ERROR_COMMAND_EXECUTION.apply(e.getMessage()));
        }
    }

    private void searchCommand(ICommandContext ctx) throws CommandSenderTypeException {
        var storage = resolveDatabase(ctx);
        var query = ctx.getTyped("query", String.class);
        var page = ctx.getFlag("page", 1);
        var commandSearch = ctx.hasFlag("command");

        var commands = commandSearch
                ? storage.getCommandsBy(ICommandDatabaseService.CommandSearchMode.COMMAND, query)
                : storage.getCommandsBy(ICommandDatabaseService.CommandSearchMode.DESCRIPTION, query);

        QUERY_PAGINATOR.generatePage(ctx, commands, page).sendTo(CommandStoreMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private ICommandDatabaseService resolveDatabase(ICommandContext ctx) throws CommandSenderTypeException {
        var server = ctx.argAtOrDefault(0, "").equalsIgnoreCase("server");
        if (server || ctx.isConsole()) return commandStorageService.getServerStorage();
        return commandStorageService.getUserStorage(ctx.asPlayer().getUniqueId());
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

        CommandBuilder.of("scmd", "savecmd", "addcmd", "acmd", "sc")
                .description("Saves a command to the command storage")
                .flag("confirm", "Confirm the saving of this command")
                .permission("greenfieldcore.commandstorage.add")
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Description of the command"))
                    .then("command", new CommandArgument()).executes(this::addCommand)
                .end()
                .then("server").permission("greenfieldcore.commandstorage.add.server")
                    .then("description", PdkArgumentTypes.quotedString(false, () -> "Description of the command"))
                        .then("command", new CommandArgument()).executes(this::addCommand)
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("rcmd", "remcmd", "delcmd", "dcmd", "rc")
                .description("Removes a command from the command storage")
                .hiddenFlag("confirm", "Confirm the removal of this command")
                .permission("greenfieldcore.commandstorage.remove")
                .then("commandId", new CommandIdArgument(commandStorageService, false, false)).executes(this::removeCommand)
                .then("server").permission("greenfieldcore.commandstorage.remove.server")
                    .then("commandId", new CommandIdArgument(commandStorageService, false, true)).executes(this::removeCommand)
                .end()
                .register(plugin);

        CommandBuilder.of("lcmd", "listcmd", "getcmd", "gcmd", "cmds", "lc")
                .description("Lists commands from the command storage")
                .flag("frequency", "If this should list the commands by most frequently used")
                .flag("page", "What page to view of the command list", PdkArgumentTypes.integer(1, () -> "The page to view"))
                .permission("greenfieldcore.commandstorage.list")
                .then("server").permission("greenfieldcore.commandstorage.list.server").executes(this::listCommands)
                .canExecute(this::listCommands)
                .register(plugin);

        CommandBuilder.of("ecmd", "editcmd", "modcmd", "mcmd")
                .description("Edits a command in the command storage")
                .hiddenFlag("confirm", "Confirm the editing of this command")
                .permission("greenfieldcore.commandstorage.edit")
                .then("commandId", new CommandIdArgument(commandStorageService, false, false))
                    .then("description")
                        .then("description", new EditDescriptionArgument(commandStorageService)).executes(this::editCommand)
                    .end()
                    .then("command")
                        .then("command", new EditCommandArgument(commandStorageService)).executes(this::editCommand)
                    .end()
                .end()
                .then("server").permission("greenfieldcore.commandstorage.edit.server")
                    .then("commandId", new CommandIdArgument(commandStorageService, false, true))
                        .then("description")
                            .then("description", new EditDescriptionArgument(commandStorageService)).executes(this::editCommand)
                        .end()
                        .then("command")
                            .then("command", new EditCommandArgument(commandStorageService)).executes(this::editCommand)
                        .end()
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("wcmd")
                .description("Internal usage.")
                .permission("greenfieldcore.commandstorage.run")
                .then("commandId", new CommandIdArgument(commandStorageService, true, false)).executes(this::wrapCommand)
                .then("server").permission("greenfieldcore.commandstorage.run.server")
                    .then("commandId", new CommandIdArgument(commandStorageService, true, true)).executes(this::wrapCommand)
                .end()
                .register(plugin);

        CommandBuilder.of("fcmd", "findcmd", "searchcmd", "fc")
                .description("Searches for commands in the command storage")
                .flag("command", "If this should find matches in command strings instead of descriptions")
                .flag("page", "What page to view of the command list", PdkArgumentTypes.integer(1, () -> "The page to view"))
                .permission("greenfieldcore.commandstorage.search")
                .then("query", PdkArgumentTypes.quotedString(false, () -> "The query to search for")).executes(this::searchCommand)
                .then("server").permission("greenfieldcore.commandstorage.search.server")
                    .then("query", PdkArgumentTypes.quotedString(false, () -> "The query to search for")).executes(this::searchCommand)
                .end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
