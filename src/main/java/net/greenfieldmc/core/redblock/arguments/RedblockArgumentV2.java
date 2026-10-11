package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.redblock.services.IRedblockServiceV2;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RedblockArgumentV2 extends AbstractStringTypedArgument<GfRedblock> {

    private static final DynamicCommandExceptionType REDBLOCK_NOT_FOUND = new DynamicCommandExceptionType(key -> () -> "Redblock " + key.toString() + " not found");

    private final IRedblockServiceV2 redblockService;

    public RedblockArgumentV2(IRedblockServiceV2 redblockService, List<String> statusFilter) {
        this.redblockService = redblockService;
    }

    @Override
    public Map<GfRedblock, Message> listSuggestions(ICommandContext commandContext) {
        var serviceResult = redblockService.getRedblocks().join();
        if (serviceResult.isFailure()) {
            return Map.of();
        }
        return serviceResult.getData().stream().collect(java.util.stream.Collectors.toMap(redblock -> redblock, redblock -> redblock::getMessage));
    }

    @Override
    public String convertToNative(GfRedblock gfRedblock) {
        return gfRedblock.getKey();
    }

    @Override
    public GfRedblock convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var redblockResult = redblockService.getRedblock(nativeType).join();
        if (redblockResult.isFailure()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw REDBLOCK_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return redblockResult.getData();
    }
}
