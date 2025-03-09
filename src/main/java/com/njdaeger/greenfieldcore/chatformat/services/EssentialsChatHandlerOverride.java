package com.njdaeger.greenfieldcore.chatformat.services;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.chat.EssentialsChat;
import com.earth2me.essentials.chat.processing.AbstractChatHandler;
import com.njdaeger.greenfieldcore.chatformat.ChatFormatModule;
import com.njdaeger.pdk.utils.Pair;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.ess3.provider.AbstractChatEvent;
import net.ess3.provider.providers.PaperChatEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.IdentityHashMap;
import java.util.Map;

public class EssentialsChatHandlerOverride extends AbstractChatHandler {

    private final IChatConfigService config;
    private final Plugin plugin;

    protected EssentialsChatHandlerOverride(Plugin plugin, Essentials ess, EssentialsChat essChat, IChatConfigService config) {
        super(ess, essChat);
        this.plugin = plugin;
        this.config = config;
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(true), plugin);
    }

    public final class ChatListener implements Listener {

        private final Map<AsyncChatEvent, PaperChatEvent> eventMap = new IdentityHashMap<>();
        private final LegacyComponentSerializer serializer;
        private final boolean formatParsing;

        public ChatListener(boolean formatParsing) {
            this.formatParsing = formatParsing;
            this.serializer = LegacyComponentSerializer.builder()
                    .flattener(ComponentFlattener.basic())
                    .extractUrls(AbstractChatEvent.URL_PATTERN)
                    .useUnusualXRepeatedCharacterHexFormat().build();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChatLowest(AsyncChatEvent event) {
            handleChatFormat(wrap(event));
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onChatNormal(AsyncChatEvent event) {
            handleChatRecipients(wrap(event));
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChatHighest(AsyncChatEvent event) {
            var paperEvent  = wrap(event);
            handleChatPostFormat(paperEvent);
            handleChatSubmit(paperEvent);

            if (event.isCancelled()) return;

            if (!formatParsing) return;

            var format = ChatFormatModule.formatString(paperEvent.getFormat().replace('&', '§'), false);
            var message = ChatFormatModule.formatString(paperEvent.getMessage().replace('&', '§'), false);

            if (event.getPlayer().hasPermission("greenfieldcore.chat.mention")) {
                var mentions = ChatFormatModule.getMentionIndices(paperEvent.getMessage());
                mentions.values().stream().map(Pair::getSecond).filter(config::allowsMentions).forEach(player -> player.playSound(player.getLocation(), config.getSound(player), config.getVolume(player), 1));
            }

            event.renderer(ChatRenderer.viewerUnaware((player, displayName, msg) ->
                    format.replaceText(builder ->
                        builder.match("%(\\d)\\$s").replacement((index, match) -> {
                            if (index.group(1).equals("1")) return displayName;
                            else return message;
                        }))
            ));
        }

        private PaperChatEvent wrap(final AsyncChatEvent event) {
            PaperChatEvent paperChatEvent = eventMap.get(event);
            if (paperChatEvent != null) {
                return paperChatEvent;
            }

            paperChatEvent = new PaperChatEvent(event, serializer);
            eventMap.put(event, paperChatEvent);

            return paperChatEvent;
        }

    }

}
