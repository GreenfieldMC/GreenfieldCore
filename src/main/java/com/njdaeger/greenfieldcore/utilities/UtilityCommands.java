package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.testresult.PlayerParser;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.stream.Stream;

import static org.bukkit.ChatColor.*;

public class UtilityCommands {

    private final UtilitiesModule module;

    public UtilityCommands(UtilitiesModule module, GreenfieldCore plugin) {
        this.module = module;
        CommandBuilder.of("nv")
                .permissions("greenfieldcore.nightvision")
                .usage("/nv")
                .description("Enable or disable night vision.")
                .executor(this::nightVision)
                .max(0)
                .build().register(plugin);

        CommandBuilder.of("convert")
                .permissions("greenfieldcore.convert")
                .usage("/convert <from> <to> <number>")
                .description("Convert from one length to another length")
                .executor(this::convert)
                .completer(this::convertTab)
                .max(3)
                .min(3)
                .build().register(plugin);

        CommandBuilder.of("badblue")
                .permissions("greenfieldcore.badblue")
                .usage("/badblue")
                .description("Nothing")
                .executor(this::badBlue)
                .max(0)
                .build().register(plugin);

        CommandBuilder.of("void")
                .permissions("greenfieldcore.void")
                .usage("/void <player>")
                .description("Void your friends!")
                .executor(this::voidCommand)
                .completer(c -> {
                    c.playerCompletionAt(0);
                })
                .max(1)
                .min(1)
                .build().register(plugin);

    }

    private void nightVision(CommandContext context) throws PDKCommandException {
        if (!context.isPlayer()) context.error(RED + "You must be a player to run this command.");
        Player player = context.asPlayer();
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(LIGHT_PURPLE + "[Utilities] " + GRAY + "Night vision disabled.");
        } else {
            player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 1), true);
            player.sendMessage(LIGHT_PURPLE + "[Utilities] " + GRAY + "Night vision enabled.");
        }
    }

    private void convert(CommandContext context) throws PDKCommandException {
        LengthType from = context.argAt(0, LengthParsedType.class);
        LengthType to = context.argAt(1, LengthParsedType.class);
        if (!context.isDoubleAt(2)) context.error(RED + "You have not provided a number.");
        double number = context.doubleAt(2);
        double conversion = from.convertTo(to, number);
        context.send(LIGHT_PURPLE + "[Utilities] " + GRAY + "{0} {1} is {2} {3}", number, number == 1 ? from.singularName : from.pluralName, conversion, conversion == 1 ? to.singularName : to.pluralName);
    }

    private void convertTab(TabContext context) {
        context.completionIf(p -> context.getLength() < 3, Stream.of(LengthType.values()).map(LengthType::name).map(String::toLowerCase).toArray(String[]::new));
        String current = context.getCurrent();
        context.completionAt(2, current + "1", current + "2", current + "3", current + "4", current + "5", current + "6", current + "7", current + "8", current + "9", current + "0", (context.getCurrent().contains(".") ? "" : current + "."));
    }

    private void voidCommand(CommandContext context) throws PDKCommandException {
        Player voidee = context.argAt(0, PlayerParser.class);
        voidee.teleport(new Location(voidee.getLocation().getWorld(), voidee.getLocation().getX(), -10000000, voidee.getLocation().getZ()));
        Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
            voidee.teleport(new Location(voidee.getLocation().getWorld(), voidee.getLocation().getX(), -20000000, voidee.getLocation().getZ()));
        }, 54);
    }

    private void badBlue(CommandContext context) throws PDKCommandException {
        if (context.getSender().getName().equalsIgnoreCase("Bluecolty")) context.error("Unknown command. Try /help for a list of commands.");
        context.send(LIGHT_PURPLE  + "[Utilities] " + GRAY + "Bad blue is now " + (module.isBadBlue() ? "disabled." : "enabled."));
        module.setBadBlue(!module.isBadBlue());
    }

}
