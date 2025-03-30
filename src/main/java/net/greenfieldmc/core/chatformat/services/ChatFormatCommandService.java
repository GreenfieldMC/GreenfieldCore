package net.greenfieldmc.core.chatformat.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class ChatFormatCommandService extends ModuleService<ChatFormatCommandService> implements IModuleService<ChatFormatCommandService> {

    private final IChatConfigService config;

    public ChatFormatCommandService(Plugin plugin, Module module, IChatConfigService config) {
        super(plugin, module);
        this.config = config;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("togglepings", "togglementions")
                .description("Enables the ping sounds when you are mentioned in chat.")
                .permission("greenfieldcore.chat.toggle-mentions")
                .canExecute(ctx -> {
                    var player = ctx.asPlayer();
                    config.setAllowMentions(player, !config.allowsMentions(player));
                    ctx.send(moduleMessage("Chat").append(Component.text(config.allowsMentions(player) ? "Enabled Chat Pings." : "Disabled Chat Pings.", NamedTextColor.GRAY)));
                })
                .then("forUser", PdkArgumentTypes.player()).permission("greenfieldcore.chat.toggle-mentions.others").executes(ctx -> {
                    var player = ctx.getTyped("forUser", Player.class);
                    config.setAllowMentions(player, !config.allowsMentions(player));
                    ctx.send(moduleMessage("Chat").append(Component.text(config.allowsMentions(player) ? "Enabled Chat Pings for " + player.getName() : "Disabled Chat Pings for " + player.getName(), NamedTextColor.GRAY)));
                })
                .register(plugin);

        CommandBuilder.of("pings", "mentions")
                .description("Set ping settings for chat pings.")
                .permission("greenfieldcore.chat.mention")
                .then("volume").then("volumeLevel", PdkArgumentTypes.floatArg(0.1f, 2.0f)).executes(ctx -> {
                    var vol = ctx.getTyped("volumeLevel", Float.class);
                    config.setVolume(ctx.asPlayer(), vol);
                    ctx.send(moduleMessage("Chat").append(Component.text("Your ping volume has been set to " + vol, NamedTextColor.GRAY)));
                }).end()
                .then("sound").then("soundChoice", ArgumentTypes.resource(RegistryKey.SOUND_EVENT)).executes(ctx -> {
                    var sound = ctx.getTyped("soundChoice", Sound.class);
                    config.setSound(ctx.asPlayer(), sound);
                    ctx.send(moduleMessage("Chat").append(Component.text("Your ping sound has been set to " + sound, NamedTextColor.GRAY)));
                }).then("reset").executes(ctx -> {
                    config.setSound(ctx.asPlayer(), Sound.BLOCK_NOTE_BLOCK_CHIME);
                    ctx.send(moduleMessage("Chat").append(Component.text("Your ping sound has been reset to the default.", NamedTextColor.GRAY)));
                }).end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
