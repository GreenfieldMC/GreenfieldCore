package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.ArgumentParseException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.stream.IntStream;

public class PageFlag extends Flag<Integer> {
    
    private final Module module;
    
    public PageFlag(Module module) {
        super(Integer.class, "Specify a page number to search.", "-page <pageNum>", "page");
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
        if (module instanceof CommandStoreModule) {
            AbstractCommandStorage storage = (context.hasFlag("s") || context.isConsole()) && context.hasPermission("greenfieldcore.commandstorage.search.server") ? ((CommandStoreModule) module).getServerStorage() : ((CommandStoreModule) module).getUserStorage(context.asPlayer().getUniqueId());
            context.completion(IntStream.rangeClosed(1, (int)Math.ceil(storage.getCommands().size()/8.)).mapToObj(String::valueOf).toArray(String[]::new));
        }
    }
}
