package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.redblock.services.IRedblockService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.BasePdkArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RedblockArgument extends BasePdkArgumentType<Redblock, Integer> {

    private static final DynamicCommandExceptionType REDBLOCK_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Redblock " + o.toString() + " not found");

    private final IRedblockService redblockService;
    private final Predicate<Redblock> filter;

    public RedblockArgument(IRedblockService redblockService, Predicate<Redblock> filter) {
        this.redblockService = redblockService;
        this.filter = filter;
    }

    @Override
    public Map<Redblock, Message> listSuggestions(ICommandContext commandContext) {
        var storage = this.redblockService.getRedblocks(filter);
        return storage.stream().collect(Collectors.toMap(s -> s, (rb) -> rb::getContent));
    }

    @Override
    public Integer convertToNative(Redblock redblock) {
        return redblock.getId();
    }

    @Override
    public Redblock convertToCustom(CommandSender sender, Integer nativeType, StringReader reader) throws CommandSyntaxException {
        var redblock = redblockService.getRedblock(nativeType);
        if (redblock == null) {
            reader.setCursor(reader.getCursor() - String.valueOf(nativeType).length());
            throw REDBLOCK_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return redblock;
    }

    @Override
    public ArgumentType<Integer> getNativeType() {
        return IntegerArgumentType.integer(0, Integer.MAX_VALUE);
    }
}
