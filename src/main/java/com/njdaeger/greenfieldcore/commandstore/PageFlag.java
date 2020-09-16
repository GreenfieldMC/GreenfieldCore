package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.ArgumentParseException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.stream.IntStream;

public class PageFlag extends Flag<Integer> {
    
    private final CommandStoreModule module;
    
    public PageFlag(CommandStoreModule module) {
        super(Integer.class, "Specify a page number to search.", "-p <pageNum>", "p");
        this.module = module;
    }
    
    @Override
    public Integer parse(CommandContext context, String argument) throws PDKCommandException {
        int parsed;
        try {
            parsed = Integer.parseInt(argument);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Integer argument unable to be parsed. Input: " + argument, true);
        }
        return parsed;
    }
    
    @Override
    public void complete(TabContext context) throws PDKCommandException {
        AbstractCommandStorage storage = (context.hasFlag("s") || context.isConsole()) && context.hasPermission("greenfieldcore.commandstorage.search.server") ? module.getServerStorage() : module.getUserStorage(context.asPlayer().getUniqueId());
        context.completion(IntStream.rangeClosed(1, (int)Math.ceil(storage.getCommands().size()/8.)).mapToObj(String::valueOf).toArray(String[]::new));
    }
}
