package com.njdaeger.greenfieldcore.chatformat.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.chatformat.ChatFormatModule;
import com.njdaeger.pdk.utils.Pair;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class DefaultChatService extends ModuleService<DefaultChatService> implements IModuleService<DefaultChatService>, Listener {

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().build();
    private final IChatConfigService config;

    public DefaultChatService(Plugin plugin, Module module, IChatConfigService config) {
        super(plugin, module);
        this.config = config;
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        var message = serializer.serialize(event.message());
        var formattedMessage = ChatFormatModule.formatString(message.replace('&', '§'), false);
        if (event.getPlayer().hasPermission("greenfieldcore.chat.mention")) {
            var mentions = ChatFormatModule.getMentionIndices(message);
            mentions.values().stream().map(Pair::getSecond).filter(config::allowsMentions).forEach(player -> player.playSound(player.getLocation(), config.getSound(player), config.getVolume(player), 1));
        }
        event.message(formattedMessage);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
