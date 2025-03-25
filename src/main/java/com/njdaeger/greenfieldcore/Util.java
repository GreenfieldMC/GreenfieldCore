package com.njdaeger.greenfieldcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.GRAY;

public final class Util {

    public static Map<UUID, String> userNameMap = new ConcurrentHashMap<>();
    public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static String formatDate(long time) {
        return DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(time)));
    }

    /**
     * Broadcasts a message to the players on the server if they have permission to see the message.
     * @param message The message to send
     * @param permission The permission to check
     */
    public static void broadcast(String message, String permission) {
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) player.sendMessage(message);
        }
    }

    public static void notAllowed(Entity entity) {
        entity.sendMessage(LIGHT_PURPLE + "[OpenServer] " + GRAY + "You cannot do that when the server is locked.");
    }

    public static int getSubstringIndex(int maxPixelWidth, String text) {
        int currentWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            if (currentWidth >= maxPixelWidth) return i - 1;
            else currentWidth += MinecraftFont.Font.getChar(text.charAt(i)).getWidth();
        }
        return text.length();
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
