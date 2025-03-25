package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class UtilityCommands {

    public UtilityCommands(GreenfieldCore plugin) {
        CommandBuilder.of("nv")
            .permission("greenfieldcore.nightvision")
            .description("Enable or disable night vision.")
            .canExecute((ctx) -> {
                var player = ctx.asPlayer();
                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    player.sendMessage(Component.text("Night vision disabled.", NamedTextColor.GRAY));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
                    player.sendMessage(Component.text("Night vision enabled.", NamedTextColor.GRAY));
                }
            }).register(plugin);

        CommandBuilder.of("void")
            .permission("greenfieldcore.void")
            .description("Void your friends!")
            .then("player", PdkArgumentTypes.player())
            .executes((ctx) -> {
                var player = ctx.getTyped("player", Player.class);
                player.teleport(new Location(player.getLocation().getWorld(), player.getLocation().getX(), -10000000, player.getLocation().getZ()));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(new Location(player.getLocation().getWorld(), player.getLocation().getX(), -20000000, player.getLocation().getZ()));
                }, 54);
            }).register(plugin);
    }
}
