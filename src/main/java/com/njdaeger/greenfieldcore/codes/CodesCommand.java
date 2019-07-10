package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.bci.types.defaults.IntegerType;
import com.njdaeger.greenfieldcore.GreenfieldCore;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class CodesCommand {

    private final CodesModule codes;
    private final CodesConfig config;
    private final GreenfieldCore plugin;

    public CodesCommand(GreenfieldCore plugin, CodesModule codes) {
        plugin.registerCommand(BCIBuilder.create("codes")
                .aliases("buildcodes", "bcodes")
                .executor(this::codes)
                .completer(this::completer)
                .permissions("greenfieldcore.codes.command")
                .description("List or change the server codes")
                .usage("/codes [add|remove|<page>] [<newCode>|<codeNumber>]")
                .build());
        this.codes = codes;
        this.config = codes.getConfig();
        this.plugin = plugin;
    }

    private void codes(CommandContext context) throws BCIException {
        if (!context.hasArgs() || context.isArgAt(0, IntegerType.class)) {
            codes.getCodes().sendTo(context.getSender(), context.argAt(0, IntegerType.class, 1));
            return;
        }
        if (context.argAt(0).equalsIgnoreCase("add")) {
            if (!context.hasPermission("greenfieldcore.codes.add")) context.noPermission();
            if (!context.hasArgAt(1)) throw new BCIException(RED + "Please specify a code to add.");
            //add the code
            return;
        }
        if (context.argAt(0).equalsIgnoreCase("remove")) {
            if (!context.hasPermission("greenfieldcore.codes.remove")) context.noPermission();
            if (!context.isArgAt(1, IntegerType.class)) throw new BCIException(RED + "Please specify a code to remove.");
            //remove the code
            //TODO somehow show the code theyre going to remove before they remove it????? maybe actionbars.....
            return;
        }
        throw new BCIException(RED + "Unknown argument given. Please specify a page or another argument.");
    }

    private void completer(TabContext context) {
        List<String> first = new ArrayList<>();
        if (context.hasPermission("greenfieldcore.codes.add")) first.add("add");
        if (context.hasPermission("greenfieldcore.codes.remove")) first.add("remove");
        context.completionIf(ctx -> ctx.isLength(1), (f) -> first);//TODO complete page numbers
    }

}
