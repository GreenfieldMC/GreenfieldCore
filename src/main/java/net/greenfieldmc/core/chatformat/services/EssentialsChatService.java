package net.greenfieldmc.core.chatformat.services;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.chat.EssentialsChat;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class EssentialsChatService extends ModuleService<EssentialsChatService> implements IModuleService<EssentialsChatService> {

    private final IChatConfigService config;

    public EssentialsChatService(Plugin plugin, Module module, IChatConfigService config) {
        super(plugin, module);
        this.config = config;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("EssentialsChat") != null && Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            try {
                module.getLogger().info("Attempting to override EssentialsChat chat handler.");
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

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

}
