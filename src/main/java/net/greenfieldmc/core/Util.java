package net.greenfieldmc.core;

import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Util {

    public static Map<UUID, String> userNameMap = new ConcurrentHashMap<>();
    public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static String formatDate(long time) {
        return DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(time)));
    }

    public static Map<UUID, String> getAllPlayers() {
        var map = new HashMap<>(Util.userNameMap);

        Bukkit.getWhitelistedPlayers().stream()
                .filter(op -> op.getName() != null)
                .forEach(op -> map.putIfAbsent(op.getUniqueId(), op.getName()));

        Bukkit.getOnlinePlayers().forEach(op -> map.put(op.getUniqueId(), op.getName()));
        return map;
    }

    public static String resolvePlayerName(UUID uuid) {
        var maybePlayer = Bukkit.getPlayer(uuid);
        if (maybePlayer != null) return maybePlayer.getName();
        return getAllPlayers().getOrDefault(uuid, "!!Unknown!!");
    }

}
