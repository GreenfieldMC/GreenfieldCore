package com.njdaeger.greenfieldcore.redblock.flags;

import com.njdaeger.greenfieldcore.redblock.RedblockUtils;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UUIDFlag extends Flag<UUID> {

    private static final Stream<String> players = Bukkit.getWhitelistedPlayers().stream()
            .map(OfflinePlayer::getName)
            .filter(Objects::nonNull);

    public UUIDFlag(String description, String usage, String aliases) {
        super(UUID.class, description, usage, aliases);
    }

    public UUIDFlag(String description, String usage, String aliases, Predicate<TabContext> when) {
        super(when, UUID.class, description, usage, aliases);
    }

    @Override
    public UUID parse(CommandContext context, String argument) throws PDKCommandException {
        final UUID[] uuid = new UUID[1];
        if (argument == null || argument.isEmpty()) return null;
        //find the argument in the list of whitelisted players and return the UUID of that player
        players.filter(name -> name.equalsIgnoreCase(argument))
                .findFirst()
                .ifPresent(name -> uuid[0] = Bukkit.getOfflinePlayer(name).getUniqueId());
        if (uuid[0] == null) return null;
        return uuid[0];
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(RedblockUtils.userNameMap.values().toArray(String[]::new));
//        context.completion(Bukkit.getWhitelistedPlayers().stream()
//                .map(OfflinePlayer::getName)
//                .filter(Objects::nonNull)
//                .toArray(String[]::new));
    }
}
