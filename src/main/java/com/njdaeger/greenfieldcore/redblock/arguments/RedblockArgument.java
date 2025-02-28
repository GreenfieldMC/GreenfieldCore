package com.njdaeger.greenfieldcore.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.redblock.Redblock;
import com.njdaeger.greenfieldcore.redblock.RedblockStorage;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.BasePdkArgumentType;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RedblockArgument extends BasePdkArgumentType<Redblock, Integer> {

    private static final DynamicCommandExceptionType REDBLOCK_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Redblock " + o.toString() + " not found");

    private final RedblockStorage storage;
    private final Predicate<Redblock> filter;

    public RedblockArgument(RedblockStorage storage, Predicate<Redblock> filter) {
        this.storage = storage;
        this.filter = filter;
    }

    @Override
    public Map<Redblock, Message> listSuggestions(ICommandContext commandContext) {
        var storage = this.storage.getRedblocksFiltered(filter);
        return storage.stream().collect(Collectors.toMap(s -> s, (rb) -> rb::getContent));
    }

    @Override
    public Integer convertToNative(Redblock redblock) {
        return redblock.getId();
    }

    @Override
    public Redblock convertToCustom(Integer nativeType, StringReader reader) throws CommandSyntaxException {
        var redblock = storage.getRedblock(nativeType);
        if (redblock == null) throw REDBLOCK_NOT_FOUND.createWithContext(reader, nativeType);
        return storage.getRedblock(nativeType);
    }

    @Override
    public ArgumentType<Integer> getNativeType() {
        return IntegerArgumentType.integer(0, Integer.MAX_VALUE);
    }
}
