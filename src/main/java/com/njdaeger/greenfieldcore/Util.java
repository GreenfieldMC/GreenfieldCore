package com.njdaeger.greenfieldcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Util {

    /**
     * Broadcasts a message to the players on the server if they have permission to see the message.
     * @param message The message to send
     * @param permission The permission to check
     */
    public static void broadcast(String message, String permission) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) player.sendMessage(message);
        }
    }
}
