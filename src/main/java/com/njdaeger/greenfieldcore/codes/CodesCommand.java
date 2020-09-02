package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.types.IntegerType;
import com.njdaeger.pdk.utils.ActionBar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bukkit.ChatColor.*;

public class CodesCommand {

    private final CodesModule codes;
    private final CodesConfig config;

    public CodesCommand(GreenfieldCore plugin, CodesModule codes) {
        CommandBuilder.of("codes", "buildcodes", "bcodes")
                .executor(this::codes)
                .completer(this::completer)
                .permissions("greenfieldcore.codes")
                .description("List or change the server codes")
                .usage("/codes [add|remove|<page>] [<newCode>|<codeNumber>]")
                .build().register(plugin);
        this.codes = codes;
        this.config = codes.getConfig();
    }

    private void codes(CommandContext context) throws PDKCommandException {
        if (context.subCommand(ctx -> !ctx.hasArgs() || ctx.isArgAt(0, IntegerType.class), this::codesList)) return;
        if (context.subCommandAt(0, "reload", true, (ctx) -> {
            config.reload();
            context.send(LIGHT_PURPLE + "[Codes] " + GRAY + "Reloaded Codes config.");
        })) return;
        if (context.subCommandAt(0, "add", true, this::addCode)) return;
        if (context.subCommandAt(0, "remove", true, this::removeCode)) return;
        else context.error(RED + "Unknown argument given. Please specify a page or another argument.");
    }

    /**
     * List the codes
     */
    private void codesList(CommandContext context) throws PDKCommandException {
        if (config.getCodes().isEmpty()) context.error(RED + "There are no codes to show.");
        else codes.getCodes().sendTo(context.getSender(), context.argAt(0, IntegerType.class, 1));
    }

    /**
     * Add a code
     */
    private void addCode(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.codes.add")) context.noPermission();
        if (!context.hasArgAt(1)) context.error(RED + "Please specify a code to add.");
        config.addCode(context.joinArgs(1));
        context.send(LIGHT_PURPLE + "[Codes] " + GRAY + "Added code #" + LIGHT_PURPLE + config.getCodes().size() + GRAY + ". \"" + ITALIC + context.joinArgs(1) + RESET + GRAY + "\".");
    }

    /**
     * Remove a code
     */
    private void removeCode(CommandContext context) throws PDKCommandException {
        if (context.getLength() > 2) context.tooManyArgs();
        if (!context.hasPermission("greenfieldcore.codes.remove")) context.noPermission();
        if (!context.isArgAt(1, IntegerType.class)) context.error(RED + "Please specify a valid code to remove.");
        int removed = context.integerAt(1) - 1;
        if (removed < 0 || removed >= config.getCodes().size()) context.error(RED + "Please specify a valid code to remove.");
        context.send(LIGHT_PURPLE + "[Codes] " + GRAY + "Removed code #" + LIGHT_PURPLE + context.argAt(1) + GRAY + ". \"" + ITALIC + config.getCode(removed) + RESET + GRAY + "\".");
        config.removeCode(removed);
    }

    /**
     * Tab completion
     */
    private void completer(TabContext context) {
        List<String> first = new ArrayList<>();
        if (context.hasPermission("greenfieldcore.codes.add")) first.add("add");
        if (context.hasPermission("greenfieldcore.codes.remove")) first.add("remove");
        if (context.hasPermission("greenfieldcore.codes.reload")) first.add("reload");
        if (!config.getCodes().isEmpty()) first.addAll(IntStream.range(1, codes.getCodes().getTotalPages() + 1).mapToObj(String::valueOf).collect(Collectors.toList()));
        context.completionIf(ctx -> ctx.isLength(1), (f) -> first);

        if (context.hasPermission("greenfieldcore.codes.remove") && context.argAt(0).equalsIgnoreCase("remove")) {
            String code = config.getCode(context.integerAt(1, 0) - 1);
            if (code == null) {
                if (context.isPlayer()) ActionBar.of(RED + "Please specify a valid code to remove").sendTo(context.asPlayer());
                else context.send(RED + "Please specify a valid code to remove");
            } else {
                if (context.isPlayer()) ActionBar.of(LIGHT_PURPLE + "[Codes] " + BLACK + "Remove: \"" + (code.length() > 60 ? code.substring(0, 60).concat("...") : code) + "\"?").sendTo(context.asPlayer());
                else context.send(LIGHT_PURPLE + "[Codes] " + GRAY + "Remove: \"" + code + "\"?");
            }
            context.completionAt(1, IntStream.range(1, config.getCodes().size() + 1).mapToObj(String::valueOf).toArray(String[]::new));
        }
    }

}
