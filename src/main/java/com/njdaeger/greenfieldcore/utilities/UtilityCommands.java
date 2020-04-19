package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.bci.SenderType;
import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.stream.Stream;

import static org.bukkit.ChatColor.*;

public class UtilityCommands {

    /*
    TODO
    /redblock <issues...>
    /getredblocks, listredblocks, redblocks
    /removeredblock <id>

    [GOTO] [INCOMPLETE|PENDING] [X] These are the issues.
    Teleport to location
    <user> is requesting completion approval
    Mark as completed and remove

     */

    public UtilityCommands(GreenfieldCore plugin) {
        plugin.registerCommand(BCIBuilder.create("nv")
                .senders(SenderType.PLAYER)
                .permissions("greenfieldcore.nightvision")
                .usage("/nv")
                .description("Enable or disable night vision.")
                .executor(this::nightVision)
                .maxArgs(0)
                .build());

        plugin.registerCommand(BCIBuilder.create("convert")
                .permissions("greenfieldcore.convert")
                .usage("/convert <from> <to> <number>")
                .description("Convert from one length to another length")
                .executor(this::convert)
                .completer(this::convertTab)
                .maxArgs(3)
                .minArgs(3)
                .build());
    }

    private void nightVision(CommandContext context) {
        Player player = context.asPlayer();
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(LIGHT_PURPLE + "[Utilities] " + GRAY + "Night vision disabled.");
        } else {
            player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 1), true);
            player.sendMessage(LIGHT_PURPLE + "[Utilities] " + GRAY + "Night vision enabled.");
        }
    }

    private void convert(CommandContext context) throws BCIException {
        LengthType from = context.argAt(0, LengthParsedType.class);
        LengthType to = context.argAt(1, LengthParsedType.class);
        if (!context.isDoubleAt(2)) throw new BCIException(RED + "You have not provided a number.");
        double number = context.doubleAt(2);
        double conversion = from.convertTo(to, number);
        context.send(LIGHT_PURPLE + "[Utilities] " + GRAY + "{0} {1} is {2} {3}", number, number == 1 ? from.singularName : from.pluralName, conversion, conversion == 1 ? to.singularName : to.pluralName);
    }

    private void convertTab(TabContext context) {
        context.completionIf(p -> context.getLength() < 3, Stream.of(LengthType.values()).map(LengthType::name).map(String::toLowerCase).toArray(String[]::new));
        String current = context.getCurrent();
        context.completionAt(2, current + "1", current + "2", current + "3", current + "4", current + "5", current + "6", current + "7", current + "8", current + "9", current + "0", (context.getCurrent().contains(".") ? "" : current + "."));
    }

}
