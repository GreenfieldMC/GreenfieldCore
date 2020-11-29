package com.njdaeger.greenfieldcore;

import net.dries007.mclink.MCLink;
import net.dries007.mclink.MCLinkAuthEvent;
import net.dries007.mclink.common.MCLinkCommon;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MCLinkIntegration extends Module implements Listener {

    private RegisteredServiceProvider<Chat> rsp;
    private String deniedMessage;
    private Set<UUID> notAllowed;

    public MCLinkIntegration(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (plugin.getServer().getPluginManager().getPlugin("MCLink") != null && plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            notAllowed = new HashSet<>();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            try {
                deniedMessage = ((MCLinkCommon)MCLink.class.getDeclaredField("common").get(MCLink.getPlugin(MCLink.class))).getConfig().getMessage(MCLinkCommon.Marker.DENIED_NO_AUTH);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                plugin.getLogger().info("Could not resolve MCLinkCommon dependency, denied messages will now be 'You must be a patron to join.'");
                deniedMessage = "You must be a patron to join.";
            }

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
        } else {
            plugin.getLogger().info("Player " + e.getPlayer().getName() + " is not allowed to join. They are not whitelisted, opped, or a patron.");
            notAllowed.add(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (notAllowed.contains(e.getPlayer().getUniqueId())) {
            e.setJoinMessage(null);
            e.getPlayer().kickPlayer(deniedMessage);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (e.getPlayer().isOp() || e.getPlayer().isWhitelisted()) return;
        if (notAllowed.contains(e.getPlayer().getUniqueId())) {
            e.setQuitMessage(null);
        }
        Chat chat = rsp.getProvider();
        chat.setPlayerPrefix(e.getPlayer(), "");
    }

}
