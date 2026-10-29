package net.greenfieldmc.core.chatformat.services;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.chat.EssentialsChat;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import java.util.logging.Logger;

/**
 * Overrides the EssentialsChat chat handler. Throws on construction if Essentials/EssentialsChat is unavailable.
 */
public class EssentialsChatService {

    public EssentialsChatService(IGreenfieldCorePlugin plugin, Logger logger, IChatConfigService config) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("EssentialsChat") != null && Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            try {
                logger.info("Attempting to override EssentialsChat chat handler.");
                var essentialsChat = Bukkit.getPluginManager().getPlugin("EssentialsChat");
                var essentials = Bukkit.getPluginManager().getPlugin("Essentials");
                AsyncChatEvent.getHandlerList().unregister(essentialsChat);

                var handler = new EssentialsChatHandlerOverride(plugin, (Essentials) essentials, (EssentialsChat) essentialsChat, config);
                handler.registerListeners();
            } catch (Exception e) {
                throw new Exception("Failed to override EssentialsChat chat handler... " + e.getMessage(), e);
            }
        } else {
            throw new Exception("Essentials and/or EssentialsChat is not enabled or found.");
        }
    }
}
