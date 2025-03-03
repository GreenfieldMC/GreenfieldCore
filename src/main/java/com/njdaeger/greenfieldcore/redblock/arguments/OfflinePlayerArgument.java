package com.njdaeger.greenfieldcore.redblock.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.Util;
import com.njdaeger.pdk.command.brigadier.arguments.BasePdkArgumentType;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.Util.getAllPlayers;

public class OfflinePlayerArgument extends BasePdkArgumentType<UUID, String> {

    private static final DynamicCommandExceptionType PLAYER_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Player " + o.toString() + " not found");

    @Override
    public String convertToNative(UUID uuid) {
        return getAllPlayers().get(uuid);
    }

    @Override
    public UUID convertToCustom(String nativeType, StringReader reader) throws CommandSyntaxException {
        if (nativeType == null || nativeType.isBlank()) throw PLAYER_NOT_FOUND.createWithContext(reader, nativeType);
        for (var entry : getAllPlayers().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nativeType)) {
                return entry.getKey();
            }
        }
        throw PLAYER_NOT_FOUND.createWithContext(reader, nativeType);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

}
