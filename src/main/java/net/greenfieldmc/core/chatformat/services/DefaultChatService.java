package net.greenfieldmc.core.chatformat.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import com.njdaeger.pdk.utils.Pair;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DefaultChatService implements Listener {

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().build();
    private final IChatConfigService config;

    public DefaultChatService(IGreenfieldCorePlugin plugin, IChatConfigService config) {
        this.config = config;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        var message = serializer.serialize(event.message());
        var formattedMessage = ChatFormatModule.formatString(message.replace('§', '&'), false);
        if (event.getPlayer().hasPermission("greenfieldcore.chat.mention")) {
            var mentions = ChatFormatModule.getMentionIndices(message);
            mentions.values().stream().map(Pair::getSecond).filter(config::allowsMentions).forEach(player -> player.playSound(player.getLocation(), config.getSound(player), config.getVolume(player), 1));
        }
        event.message(formattedMessage);
    }
}
