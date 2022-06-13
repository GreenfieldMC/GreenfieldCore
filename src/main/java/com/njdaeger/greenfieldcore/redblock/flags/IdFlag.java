package com.njdaeger.greenfieldcore.redblock.flags;

import com.njdaeger.greenfieldcore.redblock.Redblock;
import com.njdaeger.greenfieldcore.redblock.RedblockStorage;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.function.Predicate;

public class IdFlag extends Flag<Redblock> {

    private final Predicate<Redblock> filter;
    private final RedblockStorage storage;

    public IdFlag(RedblockStorage storage, Predicate<Redblock> filter) {
        super(Redblock.class, "Redblock ID", "-id <id>", "id");
        this.filter = filter;
        this.storage = storage;
    }

    @Override
    public Redblock parse(CommandContext context, String argument) throws PDKCommandException {
        int id;
        try {
            id = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new PDKCommandException("Invalid ID: " + argument, true);
        }
        var rb = storage.getRedblock(id);
        return rb == null ? null : (filter.test(storage.getRedblock(id)) ? rb : null);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(storage.getRedblocks().stream()
                .filter(filter)
                .map(Redblock::getId)
                .map(String::valueOf)
                .toArray(String[]::new));
    }
}
