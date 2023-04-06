package com.njdaeger.greenfieldcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import java.util.Random;

import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.GRAY;

public final class Util {

    public static final Random RANDOM = new Random();

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

}
