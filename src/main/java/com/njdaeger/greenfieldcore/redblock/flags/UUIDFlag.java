package com.njdaeger.greenfieldcore.redblock.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;

public class UUIDFlag extends Flag<UUID> {

    public UUIDFlag(String description, String usage, String aliases) {
        super(UUID.class, description, usage, aliases);
    }

    @Override
    public UUID parse(CommandContext context, String argument) throws PDKCommandException {
        final UUID[] uuid = new UUID[1];
        //find the argument in the list of whitelisted players and return the UUID of that player
        Bukkit.getWhitelistedPlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(name -> name.equalsIgnoreCase(argument))
                .findFirst()
                .ifPresent(name -> uuid[0] = Bukkit.getOfflinePlayer(name).getUniqueId());
        if (uuid[0] == null) return null;
        return uuid[0];
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(Bukkit.getWhitelistedPlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toArray(String[]::new));
    }
}
