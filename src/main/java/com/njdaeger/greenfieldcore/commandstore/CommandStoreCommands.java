package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.OptionalFlag;
import com.njdaeger.pdk.utils.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bukkit.ChatColor.*;

@SuppressWarnings("WeakerAccess")
public class CommandStoreCommands {
    
    private final CommandStoreModule module;
    
    public CommandStoreCommands(GreenfieldCore plugin, CommandStoreModule module) {
        this.module = module;
    
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
            .max(1)
            .description("Removes a command from command storage.")
            .usage("/lcmd [page] [-server][-frequency]")
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
                .append("[CONFIRM]").setColor(GREEN).setBold(true).clickEvent(Text.ClickAction.RUN_COMMAND, "/scmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : ""))
                .append(" to add this command. Ignore this message if you do not want to add this command.").setColor(GRAY).sendTo(context.asPlayer());
            
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
                .append("[CONFIRM]").setColor(GREEN).setBold(true).clickEvent(Text.ClickAction.RUN_COMMAND, "/rcmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : ""))
                .append(" to remove this command. Ignore this message if you do not want to remove this command.").setColor(GRAY).sendTo(context.asPlayer());
        
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
                .append("[CONFIRM]").setColor(GREEN).setBold(true).clickEvent(Text.ClickAction.RUN_COMMAND, "/ecmd " + context.joinArgs() + " -confirm" + (context.hasFlag("server") ? " -server" : ""))
                .append(" to edit this " + (part == 1 ? "command." : "description.") + " Ignore this message if you do not want to edit this " + (part == 1 ? "command." : "description.")).setColor(GRAY).sendTo(context.asPlayer());
        
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
        int page = context.integerAt(0, 1);
        int maxPage = (int)Math.ceil(storage.getCommands().size()/8.);
        if (page < 1 || maxPage < page) context.error(RED + "There are no more pages to display.");
        
        String svr = context.hasFlag("server") ? "-server " : "";
        List<AbstractCommandStorage.Command> commands;
        if (context.hasFlag("frequency")) commands = storage.getCommands().stream().sorted(Comparator.comparingInt(AbstractCommandStorage.Command::getUsed).reversed()).skip((page-1)*8).limit(8).collect(Collectors.toList());
        else commands = storage.getCommands().stream().skip((page-1)*8).limit(8).collect(Collectors.toList());
        
        Text.TextSection text = Text.of("========= ").setColor(GRAY)
            .append("CommandStorage").setColor(LIGHT_PURPLE)
            .append(" --- ").setColor(GRAY)
            .append(svr.isEmpty() ? "User" : "Server").setColor(LIGHT_PURPLE)
            .append(" =================");
        createPagedResults(false, commands, text, page, maxPage, context).sendTo(context.asPlayer());
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
        Bukkit.getLogger().info(context.getSender().getName() + " issued server command: " + command.getCommand());
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
            return lev > index ? index : lev;
        }).reversed()).skip((page-1)*8).limit(8).collect(Collectors.toList());
        
        // === CommandStorage --- User === Search Query ===
        Text.TextSection text = Text.of("=== ").setColor(GRAY).append("CommandStorage ").setColor(LIGHT_PURPLE)
            .append(" --- ").setColor(GRAY)
            .append(context.hasFlag("server") ? "Server" : "User").setColor(LIGHT_PURPLE)
            .append(" === ").setColor(GRAY)
            .append("Search Query").setUnderlined(true).setColor(LIGHT_PURPLE).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of(context.joinArgs()).setColor(GRAY))
            .append(" ===").setColor(GRAY);
        createPagedResults(true, commands, text, page, maxPage, context).sendTo(context.asPlayer());
    }
    
    private static String formatPageNum(int current, int max) {
        return String.format("%-4d/%4d", current, max);
    }
    
    private Text.TextSection createPagedResults(boolean isSearch, List<AbstractCommandStorage.Command> sortedList, Text.TextSection text, int page, int maxPage, CommandContext context) {
        String svr = context.hasFlag("server") ? "-server " : "";
        for (AbstractCommandStorage.Command cmd : sortedList) {
            text.append("\n?")
                .setColor(BLUE)
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of(cmd.getCommand()).setColor(GRAY).append("\nUses: ").append(String.valueOf(cmd.getUsed())).setColor(GRAY));
            text.append(" ");
            text.append("[E]")
                .setColor(GOLD)
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Edit this command.").setColor(GRAY))
                .clickEvent(Text.ClickAction.SUGGEST_COMMAND, "/ecmd " + svr + cmd.getId() + " command " + cmd.getCommand());
            text.append("[D]")
                .setColor(RED)
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Delete this command.").setColor(GRAY))
                .clickEvent(Text.ClickAction.RUN_COMMAND, "/rcmd " + svr + cmd.getId());
            text.append("[R]")
                .setColor(GREEN)
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Run this command.").setColor(GRAY))
                .clickEvent(Text.ClickAction.RUN_COMMAND, "/wcmd " + svr + cmd.getId());
            text.append("[C]")
                .setColor(DARK_GREEN)
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Copy this command to your clipboard.").setColor(GRAY))
                .clickEvent(Text.ClickAction.COPY_TO_CLIPBOARD, cmd.getCommand());
            text.append(" | ").setColor(LIGHT_PURPLE);
            if (cmd.getDescription().length() > 38) text.append(cmd.getDescription().substring(0, 38) + "...").hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of(cmd.getDescription()).setColor(GRAY));
            else text.append(cmd.getDescription()).setColor(GRAY);
        }
        text.append("\n========= ").setColor(GRAY);
    
        if (context.hasFlag("frequency")) svr += "-frequency ";
    
        String nextPage = isSearch ? "/fcmd -page " : "/lcmd ";
        
        if (page > 1) text.append("|<--").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + 1 + " " + svr + (isSearch ? context.joinArgs() : "")).append(" ").append("<-").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + (page - 1) + " " + svr + (isSearch ? context.joinArgs() : ""));
        else text.append("|<--").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + 0 + " " + svr + (isSearch ? context.joinArgs() : "")).append(" ").append("<-").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + 0 + " " + svr + (isSearch ? context.joinArgs() : ""));
        text.append(" ==== ").setColor(GRAY);
        text.append(" [" + formatPageNum(page, maxPage) + "]").setColor(LIGHT_PURPLE);
        text.append(" ==== ").setColor(GRAY);
        if (maxPage <= page) text.append("->").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + 0 + " " + svr + (isSearch ? context.joinArgs() : "")).append(" ").append("-->|").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + 0 + svr + (isSearch ? context.joinArgs() : ""));
        else text.append("->").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + (page + 1) + " " + svr + (isSearch ? context.joinArgs() : "")).append(" ").append("-->|").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, nextPage + maxPage + " " + svr + (isSearch ? context.joinArgs() : ""));
        text.append(" === ").setColor(GRAY);
        text.append("[☀]")
            .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of(isSearch ? "Search for another query." : "Search for a command.").setColor(GRAY))
            .clickEvent(Text.ClickAction.SUGGEST_COMMAND, "/fcmd " + (context.hasFlag("server") ? "-server " : "") + (isSearch && context.hasFlag("command") ? "-command " : ""));
        text.append(" =").setColor(GRAY);
        return text;
    }
    
}
