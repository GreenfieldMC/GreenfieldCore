package com.njdaeger.greenfieldcore;

import net.dries007.mclink.MCLinkAuthEvent;
import net.dries007.mclink.common.MCLinkCommon;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MCLinkIntegration extends Module implements Listener {

    private RegisteredServiceProvider<Chat> rsp;

    public MCLinkIntegration(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (plugin.getServer().getPluginManager().getPlugin("MCLink") != null && plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (rsp == null) plugin.getLogger().info("Could not find Chat Registration. Prefixes will not be added.");
            else plugin.getLogger().info("Using MCLink and Vault for name prefixes.");
        } else plugin.getLogger().info("Unable to use MCLink and Vault for prefix integration.");
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onAuth(MCLinkAuthEvent e) {

        if (e.getPlayer().isWhitelisted() || e.getPlayer().isOp()) return;
        if (e.getResult() == MCLinkCommon.Marker.ALLOWED) {
            Chat chat = rsp.getProvider();
            plugin.getLogger().info("Setting prefix of player " + e.getPlayer().getName());
            String world = Bukkit.getWorlds().get(0).getName();
            OfflinePlayer player = e.getPlayer();
            chat.setPlayerPrefix(world, player, "&3[$] " + chat.getGroupPrefix(world, chat.getPrimaryGroup(world, player)));
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        if (e.getPlayer().isOp() || e.getPlayer().isWhitelisted()) return;
        Chat chat = rsp.getProvider();
        chat.setPlayerPrefix(e.getPlayer(), "");
    }

}
