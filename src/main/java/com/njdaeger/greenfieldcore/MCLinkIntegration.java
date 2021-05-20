package com.njdaeger.greenfieldcore;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.utils.DateUtil;
import net.dries007.mclink.MCLinkAuthEvent;
import net.dries007.mclink.common.MCLinkCommon;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MCLinkIntegration extends Module implements Listener {

    private Essentials ess;
    private String defaultJoinMessage;
    private RegisteredServiceProvider<Chat> rsp;
    private Map<UUID, Boolean> allowedUsers;
    private Map<UUID, String> joinMessages;

    public MCLinkIntegration(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("MCLink") != null && pm.getPlugin("Vault") != null && pm.getPlugin("Essentials") != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            ess = Essentials.getPlugin(Essentials.class);
            allowedUsers = new HashMap<>();
            joinMessages = new HashMap<>();
            defaultJoinMessage = ess.getSettings().isCustomJoinMessage() ? ess.getSettings().getCustomJoinMessage() : null;
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
        //By default, whenever we start this authorization check, we want to make sure the auth flag is set to false.
        allowedUsers.put(e.getPlayer().getUniqueId(), false);
        if (e.getPlayer().isWhitelisted() || e.getPlayer().isOp()) {//If they are whitelisted or opped, by default, they are allowed on the server.
            allowedUsers.put(e.getPlayer().getUniqueId(), true);
            String joinMsg = joinMessages.get(e.getPlayer().getUniqueId());
            if (joinMsg != null) { //If this event fires AFTER the join event, we will have the formatted join message, so we can broadcast it in this event. Otherwise, essentials will handle the join message
                Bukkit.getServer().broadcastMessage(joinMsg);
            }
        }
        else if (e.getResult() == MCLinkCommon.Marker.ALLOWED) {
            Chat chat = rsp.getProvider();
            plugin.getLogger().info("Setting prefix of player " + e.getPlayer().getName());
            String world = Bukkit.getWorlds().get(0).getName();
            OfflinePlayer player = e.getPlayer();
            chat.setPlayerPrefix(world, player, "&3[$] " + chat.getGroupPrefix(world, chat.getPrimaryGroup(world, player)));
            allowedUsers.put(e.getPlayer().getUniqueId(), true);
            String joinMsg = joinMessages.get(e.getPlayer().getUniqueId());
            if (joinMsg != null) { //If this event fires AFTER the join event, we will have the formatted join message, so we can broadcast it in this event. Otherwise, essentials will handle the join message
                Bukkit.getServer().broadcastMessage(joinMsg);
            }
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        //This is called either before or after the authorization has been checked, if its done before auth, the user will not be in the map, and the user
        //is added to the map with the "auth" boolean set to false. All join messages are cancelled and cached and handled by MCLinkAuthEvent
        Player player = e.getPlayer();
        if (allowedUsers.get(player.getUniqueId()) == null) {
            allowedUsers.put(player.getUniqueId(), false);
            String joinMsg = defaultJoinMessage == null ? e.getJoinMessage() : defaultJoinMessage.replace("{PLAYER}", player.getDisplayName()).replace("{USERNAME}", player.getName())
                    .replace("{UNIQUE}", NumberFormat.getInstance().format(ess.getUserMap().getUniqueUsers()))
                    .replace("{ONLINE}", NumberFormat.getInstance().format(ess.getOnlinePlayers().size()))
                    .replace("{UPTIME}", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()));
            joinMessages.put(player.getUniqueId(), joinMsg);
            e.setJoinMessage(null);
        } else if (!allowedUsers.get(player.getUniqueId())) {//only cancel the join message if the user hasnt been authorized yet.
            e.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        if (!allowedUsers.get(e.getPlayer().getUniqueId())) {
            e.setQuitMessage(null);
            return;
        }
        allowedUsers.remove(e.getPlayer().getUniqueId());
        joinMessages.remove(e.getPlayer().getUniqueId());
        if (e.getPlayer().isOp() || e.getPlayer().isWhitelisted()) {
            return;
        }
        Chat chat = rsp.getProvider();
        chat.setPlayerPrefix(e.getPlayer(), "");
    }

}
