package net.greenfieldmc.core.utilities.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.utilities.UtilityMessages;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class UtilityCommands extends ModuleService<UtilityCommands> implements IModuleService<UtilityCommands> {


    public UtilityCommands(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("nv")
                .permission("greenfieldcore.nightvision")
                .description("Enable or disable night vision.")
                .canExecute((ctx) -> {
                    var player = ctx.asPlayer();
                    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        player.sendMessage(UtilityMessages.DISABLE_NIGHTVISION);
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
                        player.sendMessage(UtilityMessages.ENABLE_NIGHTVISION);
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

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
