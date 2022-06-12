package com.njdaeger.greenfieldcore.redblock.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.ArgumentParseException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.stream.IntStream;

public class PageFlag extends Flag<Integer> {

    public PageFlag() {
        super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), Integer.class, "Filter which page of the redblock list to view", "-page <pageNumber>", "page");
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
        try {
            Integer.parseInt(context.getCurrent());
            context.completion(IntStream.rangeClosed(0, Integer.parseInt(context.getCurrent()) * 10).mapToObj(String::valueOf).toArray(String[]::new));
        } catch (NumberFormatException ignored) {
        }
    }
}
