package net.greenfieldmc.core.shared.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.greenfieldmc.core.Util;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.BasePdkArgumentType;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class OfflinePlayerArgument extends BasePdkArgumentType<UUID, String> {

    private static final DynamicCommandExceptionType PLAYER_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Player " + o.toString() + " not found");

    @Override
    public List<UUID> listBasicSuggestions(ICommandContext commandContext) {
        return List.copyOf(Util.userNameMap.keySet());
    }

    @Override
    public String convertToNative(UUID uuid) {
        return Util.userNameMap.get(uuid);
    }

    @Override
    public UUID convertToCustom(CommandSender sender, String nativeType, StringReader reader) throws CommandSyntaxException {
        if (nativeType == null || nativeType.isBlank()) throw PLAYER_NOT_FOUND.createWithContext(reader, nativeType);
        for (var entry : Util.userNameMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nativeType)) {
                return entry.getKey();
            }
        }
        reader.setCursor(reader.getCursor() - nativeType.length());
        throw PLAYER_NOT_FOUND.createWithContext(reader, nativeType);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

}
