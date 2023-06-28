package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.OptionalFlag;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.njdaeger.greenfieldcore.Util.getSubstringIndex;
import static org.bukkit.ChatColor.*;

public class CommandStoreCommands {
    
    private final CommandStoreModule module;
    private final ChatPaginator<AbstractCommandStorage.Command, CommandContext> paginator;
    
    public CommandStoreCommands(GreenfieldCore plugin, CommandStoreModule module) {
        this.module = module;

        this.paginator = ChatPaginator.builder(this::lineGenerator)
                .addComponent((ctx, paginator, results, pg) -> {
                    if (ctx.hasFlag("server")) return Text.of("Server CommandStorage").setColor(LIGHT_PURPLE);
                    return Text.of("User CommandStorage").setColor(LIGHT_PURPLE);
                }, ComponentPosition.TOP_CENTER)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/" + ctx.getAlias() + " " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/" + ctx.getAlias() + " " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/" + ctx.getAlias() + " " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/" + ctx.getAlias() + " " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent((ctx, paginator, results, pg) -> {
                    if (ctx.getCommand().getName().equalsIgnoreCase("fcmd")) return Text.of("Query").setColor(LIGHT_PURPLE).setUnderlined(true).setHoverEvent(HoverAction.SHOW_TEXT, Text.of(ctx.joinArgs()).setColor(GRAY));
                    else return null;
                }, ComponentPosition.TOP_RIGHT)
                .addComponent((ctx, paginator, results, pg) -> {
                    var msg = "Search for a command.";
                    if (ctx.getCommand().getName().equalsIgnoreCase("fcmd")) msg = "Search for another command.";
                    return Text.of("[☀]")
                            .setColor(LIGHT_PURPLE)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(msg).setColor(GRAY))
                            .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/fcmd " + (ctx.hasFlag("server") ? "-server " : "") + (ctx.hasFlag("command") ? "command" : "")));
                }, ComponentPosition.BOTTOM_RIGHT)
                .build();
    
        CommandBuilder.of("scmd", "savecmd", "addcmd", "acmd", "sc")
            .flag(new OptionalFlag("Save the command to public storage.", "-server", "server"))
            .flag(new OptionalFlag("Confirm the addition to command storage.", "-confirm", "confirm"))
            .min(2)
            .description("Saves a command to command storage for easy reference and usage.")
            .usage("/scmd <description...> </<command [arguments]>...> [-server][-confirm]")
            .permissions("greenfieldcore.commandstorage.add.user", "greenfieldcore.commandstorage.add.server")
            .executor(this::addCommand)
            .register(plugin);
    
        CommandBuilder.of("rcmd", "remcmd", "delcmd", "dcmd", "rc")
            .flag(new OptionalFlag("Remove the command from public storage.", "-server", "server"))
            .flag(new OptionalFlag("Confirm the removal from command storage.", "-confirm", "confirm"))
            .min(1)
            .max(1)
            .description("Removes a command from command storage.")
            .usage("/rcmd <id> [-server][-confirm]")
            .permissions("greenfieldcore.commandstorage.remove.user", "greenfieldcore.commandstorage.remove.server")
            .executor(this::removeCommand)
            .completer(this::removeCommandTab)
            .register(plugin);
    
        CommandBuilder.of("lcmd", "listcmd", "getcmd", "gcmd", "cmds", "lc")
            .flag(new OptionalFlag("List commands saved to public storage.", "-server", "server"))
            .flag(new OptionalFlag("Sort the commands by most frequently used.", "-frequency", "frequency"))
            .flag(new PageFlag(module))
            .max(0)
            .description("Removes a command from command storage.")
            .usage("/lcmd [-server][-frequency] [-page <page>]")
            .permissions("greenfieldcore.commandstorage.list.user", "greenfieldcore.commandstorage.list.server")
            .executor(this::listCommands)
            .completer(this::listCommandsTab)
            .register(plugin);
    
        CommandBuilder.of("ecmd", "editcmd", "modcmd", "mcmd")
            .flag(new OptionalFlag("Edit a command from public storage.", "-server", "server"))
            .flag(new OptionalFlag("Confirm the edit of the command.", "-confirm", "confirm"))
            .min(3)
            .description("Edits an existing command in command storage.")
            .usage("/ecmd <id> <command|description> <value...> [-server][-confirm]")
            .permissions("greenfieldcore.commandstorage.edit.user", "greenfieldcore.commandstorage.edit.server")
            .executor(this::editCommand)
            .completer(this::editCommandTab)
            .register(plugin);
    
        CommandBuilder.of("fcmd", "findcmd", "searchcmd", "fc")
            .flag(new OptionalFlag("Find a command from public storage.", "-server", "server"))
            .flag(new OptionalFlag("Find matches in commands instead of descriptions.", "-command", "command"))
            .flag(new PageFlag(module))
            .min(1)
            .description("Search for a command in command storage.")
            .usage("/fcmd <query> [-page <page>] [-server][-command]")
            .permissions("greenfieldcore.commandstorage.search.user", "greenfieldcore.commandstorage.search.server")
            .executor(this::searchCommand)
            .register(plugin);
        
        CommandBuilder.of("wcmd")
            .flag(new OptionalFlag("Internal usage.", "--", "server"))
            .min(1)
            .description("Internal usage.")
            .permissions("greenfieldcore.commandstorage.run.user", "greenfieldcore.commandstorage.run.server")
            .usage("/wcmd")
            .executor(this::wrapCommand)
            .register(plugin);
    }

    // /scmd "command" "description" "shortDescription" -s -c
    // /rcmd id -s -c
    // /lcmd page -f -s
    // /ecmd id part value -s -c
    public void addCommand(CommandContext context) throws PDKCommandException {
        String[] info = context.joinArgs().split(" /", 2);
        if (info.length != 2) context.error(RED + "You must provide a command to run.");
        
        String command = "/" + info[1];
        String desc = info[0];
        
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.add.server")) context.noPermission();
        
        if (context.hasFlag("confirm")) {
            storage.addCommand(command, desc);
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Successfully added command.");
        } else {
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Confirm you wish to add this command.");
            if (context.isConsole()) {
                context.send(GRAY + "Run the add command again with the -confirm flag at the end to confirm the addition.");
            } else Text.of("Press ").setColor(GRAY)
                .appendRoot("[CONFIRM]").setColor(GREEN).setBold(true).setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/scmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : "")))
                .appendRoot(" to add this command. Ignore this message if you do not want to add this command.").setColor(GRAY).sendTo(context.asPlayer());
            
        }
    }
    
    public void removeCommand(CommandContext context) throws PDKCommandException {
        int id = context.integerAt(0);
        
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.remove.server")) context.noPermission();
        
        if (storage.getCommand(id) == null) context.error(RED + "Command ID #" + id + " does not exist.");
    
        if (context.hasFlag("confirm")) {
            storage.removeCommand(id);
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Successfully removed command.");
        } else {
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Confirm you wish to remove this command.");
            if (context.isConsole()) {
                context.send(GRAY + "Run the remove command again with the -confirm flag at the end to confirm the removal.");
            } else Text.of("Press ").setColor(GRAY)
                .appendRoot("[CONFIRM]").setColor(GREEN).setBold(true).setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/rcmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : "")))
                .appendRoot(" to remove this command. Ignore this message if you do not want to remove this command.").setColor(GRAY)
                .sendTo(context.getSender());
        }
    }
    
    public void removeCommandTab(TabContext context) {
        AbstractCommandStorage storage = (context.hasFlag("server") || context.isConsole()) && context.hasPermission("greenfieldcore.commandstorage.remove.server") ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        context.completionAt(0, storage.getCommands().stream().map(AbstractCommandStorage.Command::getId).map(String::valueOf).toArray(String[]::new));
    }
    
    public void editCommand(CommandContext context) throws PDKCommandException {
        int id = context.integerAt(0);
        
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.edit.server")) context.noPermission();
        AbstractCommandStorage.Command command = storage.getCommand(id);
        
        if (command == null) context.error(RED + "Command ID #" + id + " does not exist.");
        
        //if editing the command, the value is 1
        //if editing the description, the value is -1
        //if the value is 0 we dont know what part it is
        int part = context.argAt(1).equalsIgnoreCase("command") ? 1 : context.argAt(1).equalsIgnoreCase("description") ? -1 : 0;
        if (part == 0) context.error(RED + "Unknown command part. Valid parts are \"command\" and \"description\"");
        
        if (context.hasFlag("confirm")) {
            if (part == 1) command.setCommand(context.joinArgs(2));
            else command.setDescription(context.joinArgs(2));
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Successfully edited the " + (part == 1 ? "command." : "description."));
        } else {
            context.send(LIGHT_PURPLE + "[CommandStorage] " + GRAY + "Confirm you wish to edit this " + (part == 1 ? "command." : "description."));
            if (context.isConsole()) {
                context.send(GRAY + "Run the edit command again with the -c flag at the end to confirm the edit.");
            } else Text.of("Press ").setColor(GRAY)
                .appendRoot("[CONFIRM]").setColor(GREEN).setBold(true).setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/ecmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : "")))
                .appendRoot(" to edit this " + (part == 1 ? "command." : "description.") + " Ignore this message if you do not want to edit this " + (part == 1 ? "command." : "description.")).setColor(GRAY).sendTo(context.asPlayer());
        
        }
    }
    
    public void editCommandTab(TabContext context) throws PDKCommandException {
        AbstractCommandStorage storage = (context.hasFlag("server") || context.isConsole()) && context.hasPermission("greenfieldcore.commandstorage.edit.server") ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        context.completionAt(0, storage.getCommands().stream().map(AbstractCommandStorage.Command::getId).map(String::valueOf).toArray(String[]::new));
        context.completionAt(1, "command", "description");
        if (context.isLength(3) && context.argAt(1).equalsIgnoreCase("command")) {
            context.completionAt(2, storage.getCommand(context.integerAt(0)).getCommand());
        }
        else if (context.isLength(3) && context.argAt(1).equalsIgnoreCase("description")) {
            context.completionAt(2, storage.getCommand(context.integerAt(0)).getDescription());
        }
    }
        /*
    
    ==== CommandStorage - User ====================
    ==== CommandStorage - Server ==================
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    [i] [E][D][R][C] | Description
    ==== |<-- <- ==== [page/page] ==== -> -->| ====
    
     */
    // /lcmd page -f -s
    public void listCommands(CommandContext context) throws PDKCommandException {
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.list.server")) context.noPermission();
        int page = context.getFlag("page", 1);
//        int maxPage = (int)Math.ceil(storage.getCommands().size()/8.);
//        if (page < 1 || maxPage < page) context.error(RED + "There are no more pages to display.");

        List<AbstractCommandStorage.Command> commands;
        if (context.hasFlag("frequency")) commands = storage.getCommands().stream().sorted(Comparator.comparingInt(AbstractCommandStorage.Command::getUsed).reversed()).collect(Collectors.toList());
        else commands = new ArrayList<>(storage.getCommands());

        paginator.generatePage(context, commands, page).sendTo(Text.of("Page does not exist.").setColor(RED), context.asPlayer());
    }
    
    public void listCommandsTab(TabContext context) {
        AbstractCommandStorage storage = (context.hasFlag("server") || context.isConsole()) && context.hasPermission("greenfieldcore.commandstorage.list.server") ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        context.completionAt(0, IntStream.rangeClosed(1, (int)Math.ceil(storage.getCommands().size()/8.)).mapToObj(String::valueOf).toArray(String[]::new));
    }
    
    public void wrapCommand(CommandContext context) throws PDKCommandException {
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.run.server")) context.noPermission();
        AbstractCommandStorage.Command command = storage.getCommand(context.integerAt(0));
        if (command == null) context.error(RED + "Command ID #" + context.argAt(0) + " does not exist.");
        command.incrementUsage();
        GreenfieldCore.logger().info(context.getSender().getName() + " issued server command: " + command.getCommand());
        context.asPlayer().performCommand(command.getCommand().substring(1));
    }
    
    //fcmd -p <page> -s -c
    public void searchCommand(CommandContext context) throws PDKCommandException {
        AbstractCommandStorage storage = context.hasFlag("server") || context.isConsole() ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        if (context.hasFlag("server") && !context.hasPermission("greenfieldcore.commandstorage.search.server")) context.noPermission();
        int page = context.hasFlag("page") ? context.getFlag("page") : 1;
        int maxPage = (int)Math.ceil(storage.getCommands().size()/8.);
        if (page < 1 || maxPage < page) context.error(RED + "There are no more pages to display.");
        
        Function<AbstractCommandStorage.Command, String> command = (c) -> context.hasFlag("command") ? c.getCommand().toUpperCase() : c.getDescription().toUpperCase();
        List<AbstractCommandStorage.Command> commands = storage.getCommands().stream().sorted(Comparator.comparingInt(c -> {
            int lev = StringUtils.getLevenshteinDistance(command.apply((AbstractCommandStorage.Command)c), context.joinArgs().toUpperCase());
            int index = command.apply((AbstractCommandStorage.Command)c).indexOf(context.joinArgs().toUpperCase());
            return Math.min(lev, index);
        }).reversed()).skip((page-1)*8L).limit(8).collect(Collectors.toList());

        paginator.generatePage(context, commands, page).sendTo(Text.of("Page does not exist.").setColor(RED), context.asPlayer());
    }

    private Text.Section lineGenerator(AbstractCommandStorage.Command command, CommandContext context) {
        String svr = context.hasFlag("server") ? "-server " : "";

        var text = Text.of("? ")
                .setColor(BLUE)
                .setBold(true)
                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(command.getCommand()).setColor(GRAY).append("\nUses: " + command.getUsed()));

        if (svr.isEmpty() || context.hasPermission("greenfieldcore.commandstorage.edit.server"))
            text.appendRoot("[E]")
                    .setColor(GOLD)
                    .setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Edit this command.").setColor(GRAY))
                    .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/ecmd " + svr + command.getId() + " command " + command.getCommand()));

        if (svr.isEmpty() || context.hasPermission("greenfieldcore.commandstorage.remove.server"))
            text.appendRoot("[D]")
                    .setColor(RED)
                    .setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Delete this command.").setColor(GRAY))
                    .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/rcmd " + svr + command.getId()));

        if (svr.isEmpty() || context.hasPermission("greenfieldcore.commandstorage.run.server")) {
            text.appendRoot("[R]")
                    .setColor(GREEN)
                    .setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Run this command.").setColor(GRAY))
                    .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of(command.getCommand()));
            text.appendRoot("[C]")
                    .setColor(DARK_GREEN)
                    .setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Copy this command to your clipboard").setColor(GRAY))
                    .setClickEvent(ClickAction.COPY_TO_CLIPBOARD, ClickString.of(command.getCommand()));
        }

        text.appendRoot(" | ").setColor(GRAY);
        if (MinecraftFont.Font.getWidth(command.getDescription()) > 170) {
            text.appendRoot(command.getDescription().substring(0, getSubstringIndex(170, command.getDescription())))
                    .setColor(GRAY)
                    .append("...")
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(command.getDescription()).setColor(GRAY));
        } else {
            text.appendRoot(command.getDescription()).setColor(GRAY);
        }
        return text;
    }
    
}
