package com.njdaeger.greenfieldcore;

import com.njdaeger.authenticationhub.ApplicationRegistry;
import com.njdaeger.authenticationhub.discord.DiscordApplication;
import com.njdaeger.authenticationhub.patreon.PatreonApplication;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuthHubIntegration extends Module implements Listener {

    private RegisteredServiceProvider<Chat> rsp;
    private RegisteredServiceProvider<ApplicationRegistry> appReg;
    private ConcurrentMap<UUID, String> patreonUsersPrefixes;

    public AuthHubIntegration(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("AuthenticationHub") != null && pm.getPlugin("Vault") != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (rsp == null) plugin.getLogger().info("Could not find Chat Registration. Prefixes will not be added.");
            else plugin.getLogger().info("Using Vault for name prefixes.");
            appReg = plugin.getServer().getServicesManager().getRegistration(ApplicationRegistry.class);
            if (appReg == null) plugin.getLogger().info("Could not find ApplicationRegistry service. AuthenticationHub integration will be disabled.");
            else plugin.getLogger().info("Using AuthenticationHub for patron verification.");
            patreonUsersPrefixes = new ConcurrentHashMap<>();
        } else plugin.getLogger().info("Not all required plugins are installed. AuthHub integration will be disabled.");
    }

    @Override
    public void onDisable() {
        //if rsp is not null, run a for loop over all players in the patreonUserPrefixes map and remove their prefixes
        if (rsp != null) {
            var chat = rsp.getProvider();
            patreonUsersPrefixes.forEach((uuid, prefix) -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) chat.setPlayerPrefix(player, prefix.isEmpty() ? null : prefix);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (appReg == null) return;
        var reg = appReg.getProvider();
        var patreon = reg.getApplication(PatreonApplication.class);
        var chat = rsp == null ? null : rsp.getProvider();
        var player = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int cached = patreon.getPledgingAmountCached(e.getPlayer().getUniqueId());
            if (cached == 0) {
                cached = patreon.getPledgingAmountSync(player.getUniqueId(), patreon.getConnection(player.getUniqueId()));
            }
            if (cached >= patreon.getRequiredPledge()) {
                plugin.getLogger().info("Setting prefix of player " + player.getName());
                if (chat != null) {
                    var currentPrefix = chat.getPlayerPrefix(player) == null ? "" : chat.getPlayerPrefix(player);
                    if (currentPrefix.contains("[$]")) {
                        plugin.getLogger().info("Prefix is already set for player");
                        return;
                    }
                    patreonUsersPrefixes.put(player.getUniqueId(), currentPrefix);
                    chat.setPlayerPrefix(player, "&3[$] " + currentPrefix + chat.getGroupPrefix(player.getWorld(), chat.getPrimaryGroup(player)));
                }
            } else if (cached == -1) {
                plugin.getLogger().info("Prefix not set for player " + player.getName() + " because they are not a patron or are not linked with Patreon.");
            } else {
                plugin.getLogger().info("Prefix not set for player " + player.getName() + " because their pledge is less than " + patreon.getRequiredPledge() + " cents. [" + cached + "]");
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin_discordIntg(PlayerLoginEvent e) {
        if (appReg == null) return;
        boolean whitelist = Bukkit.hasWhitelist();
        var reg = appReg.getProvider();
        var discord = reg.getApplication(DiscordApplication.class);
        var id = e.getPlayer().getUniqueId();

        var user = discord.getConnection(id);
        if (whitelist && Bukkit.getServer().getWhitelistedPlayers().stream().anyMatch(op -> op.getUniqueId().equals(id))) {
            if (e.getPlayer().hasPermission("greenfieldcore.discord.exempt")) {
                Bukkit.getLogger().info("User " + e.getPlayer().getName() + " is exempted from having a linked discord profile.");
                return;
            }
            if (user == null) e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.notConnected", "null"));
            else if (user.isExpired()) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.expiredUser", "null"));
                discord.removeConnection(id);
            } else if (discord.isRefreshingUserToken(id)) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.refreshingUserToken", "null"));
            } else if (discord.isGettingDiscordUserProfile(id)) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.gettingDiscordProfile", "null"));
            } else if (user.isAlmostExpired()) {
                Bukkit.getLogger().info(user.getTimeUntilExpiration());
                discord.refreshUserToken(id, user);
            } else discord.getDiscordUserAsync(id, user);
            return;
        }

        //any moment after this means either the user is not whitelisted (meaning they arent required to have a discord account)
        //or the whitelist is disabled. in which case if they have an account associated with their account, we update it, if not,
        //not a big deal, we just remove the connection
        if (user == null) return;

        if (user.isExpired()) {
            e.getPlayer().sendMessage(discord.getAppConfig().getString("messages.expiredUser", "null"));
            discord.removeConnection(id);
        }

        if (user.isAlmostExpired()) {
            Bukkit.getLogger().info(user.getTimeUntilExpiration());
            discord.refreshUserToken(id, user);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if (rsp != null && patreonUsersPrefixes.containsKey(e.getPlayer().getUniqueId())) {
            var chat = rsp.getProvider();
            chat.setPlayerPrefix(e.getPlayer(), patreonUsersPrefixes.get(e.getPlayer().getUniqueId()).isEmpty() ? null : patreonUsersPrefixes.get(e.getPlayer().getUniqueId()));
            patreonUsersPrefixes.remove(e.getPlayer().getUniqueId());
        }
    }

}
