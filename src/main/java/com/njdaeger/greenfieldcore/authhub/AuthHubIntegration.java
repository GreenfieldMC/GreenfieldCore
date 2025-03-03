package com.njdaeger.greenfieldcore.authhub;

import com.njdaeger.authenticationhub.ApplicationRegistry;
import com.njdaeger.authenticationhub.ConnectionRequirement;
import com.njdaeger.authenticationhub.discord.DiscordUserLoginEvent;
import com.njdaeger.authenticationhub.patreon.PatreonApplication;
import com.njdaeger.authenticationhub.patreon.PatreonUserLoginEvent;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import io.papermc.paper.ban.BanListType;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class AuthHubIntegration extends Module implements Listener {

    private AuthhubConfig config;
    private RegisteredServiceProvider<Chat> rsp;
    private RegisteredServiceProvider<ApplicationRegistry> appReg;
    private List<UUID> prefixedUsers;

    public AuthHubIntegration(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        ProfileBanList pbl = Bukkit.getBanList(BanListType.PROFILE);
        new ConnectionRequirement("DISCORD_REQUIREMENT", (p) -> {
            if (p.hasPermission("greenfieldcore.discord.exempt")) {
                GreenfieldCore.logger().info("User " + p.getName() + " is exempted from having a linked discord profile.");
                return false;
            }
            else if (p.isWhitelisted() && !pbl.isBanned(p.getPlayerProfile())) {
                GreenfieldCore.logger().info("User " + p.getName() + " must have a linked discord profile.");
                return true;
            }
            GreenfieldCore.logger().info("User " + p.getName() + " does not need a linked discord profile - they were not found in the whitelist or they are a banned member.");
            return false;
        });

        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("AuthenticationHub") != null && pm.getPlugin("Vault") != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (rsp == null) plugin.getLogger().info("Could not find Chat Registration. Prefixes will not be added.");
            else plugin.getLogger().info("Using Vault for name prefixes.");
            appReg = plugin.getServer().getServicesManager().getRegistration(ApplicationRegistry.class);
            if (appReg == null) plugin.getLogger().info("Could not find ApplicationRegistry service. AuthenticationHub integration will be disabled.");
            else plugin.getLogger().info("Using AuthenticationHub for patron verification.");
            prefixedUsers = new ArrayList<>();
            config = new AuthhubConfig(plugin);
        } else plugin.getLogger().info("Not all required plugins are installed. AuthHub integration will be disabled.");
    }

    @Override
    public void tryDisable() {
        //if rsp is not null, run a for loop over all players in the patreonUserPrefixes map and remove their prefixes
        if (rsp != null) {
            var chat = rsp.getProvider();
            prefixedUsers.forEach((uuid) -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) chat.setPlayerPrefix(player, null);
            });
        }
    }

    @EventHandler
    public void onDiscordLogin(DiscordUserLoginEvent e) {
        if (!e.getPlayer().isBanned()) e.allow();
    }

    @EventHandler
    public void onPatreonLogin(PatreonUserLoginEvent e) {
        if (e.getUser().getPledgingAmount() < config.getRequiredPatreonPledge() && e.getApplication().getConnectionRequirement().isRequired(e.getPlayer())) {
            e.disallow("Your patron account currently pledges " + e.getUser().getPledgingAmount() + " cents, which is less than the required " + config.getRequiredPatreonPledge() + " cents. Please upgrade your patronage to continue.");
        } else {
            e.allow();
            if (rsp != null) {
                var chat = rsp.getProvider();
                var player = e.getPlayer();
                var currentPrefix = chat.getPlayerPrefix(player) == null ? "" : chat.getPlayerPrefix(player);
                if (currentPrefix.contains("&3[$]")) {
                    plugin.getLogger().info("Prefix is " + currentPrefix + " for player " + player.getName());
//                    plugin.getLogger().info("Prefix is already set for player");
                    return;
                }
                var newPfx = currentPrefix.isEmpty() ? "&3[$]" : "&3[$] " + currentPrefix;
                chat.setPlayerPrefix(player, newPfx + chat.getGroupPrefix(player.getWorld(), chat.getPrimaryGroup(player)));
                prefixedUsers.add(player.getUniqueId());
            } else plugin.getLogger().warning("Chat provider is null. Prefixes will not be added.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if (rsp != null && prefixedUsers.contains(e.getPlayer().getUniqueId())) {
            var chat = rsp == null ? null : rsp.getProvider();
            if (chat == null) return;
            var pfx = chat.getPlayerPrefix(e.getPlayer());
            if (pfx == null) return;
            if (!pfx.contains("&3[$]")) return;
            var replacement = pfx.replace("&3[$] ", ""); //first attempt to find the prefix with a space after the dollar sign
            replacement = pfx.replace("&3[$]", ""); //if that fails, try to find the prefix without a space after the dollar sign
            chat.setPlayerPrefix(e.getPlayer(), replacement.isEmpty() ? null : replacement);
            prefixedUsers.remove(e.getPlayer().getUniqueId());
        }
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPlayerLogin_patreonIntg(PlayerLoginEvent e) {
//        if (appReg == null) return;
//        var reg = appReg.getProvider();
//        var chat = rsp == null ? null : rsp.getProvider();
//        var patreon = reg.getApplication(PatreonApplication.class);
//
//        if (patreon == null) {
//            plugin.getLogger().warning("Patreon application not enabled. Patreon integration will not work.");
//            return;
//        }
//
//        //the patreon listener in the authentication hub kicks the user if we dont have a cached value, a connected account, or if the value is being refreshed.
//        //so at this point, if that listener doesnt disallow the connection, we definitely have their information cached, so we should be alright.
//        if (patreon.getConnectionRequirement().isRequired(e.getPlayer())) {
//            System.out.println("test1");
//            int cached = patreon.getPledgingAmountCached(e.getPlayer().getUniqueId());
//            System.out.println("test2");
//            if (!patreon.isPledgingAmountCached(e.getPlayer().getUniqueId()) || patreon.isGettingPledgeStatus(e.getPlayer().getUniqueId()) || patreon.isRefreshingUserToken(e.getPlayer().getUniqueId())) return;
//            System.out.println("test3");
//            if (cached < config.getRequiredPatreonPledge()) {
//                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your patron account currently pledges " + cached + " cents, which is less than the required " + config.getRequiredPatreonPledge() + " cents. Please upgrade your patronage to continue.");
//            } else {
//                e.setResult(PlayerLoginEvent.Result.ALLOWED);
//                if (chat != null) {
//                    plugin.getLogger().info("Setting prefix of player " + e.getPlayer().getName());
//                    //no reason to worry about previous prefix, they are a patron and should only ever have the dollar sign prefix
//                    chat.setPlayerPrefix(e.getPlayer(), "&3[$] " + chat.getGroupPrefix(e.getPlayer().getWorld(), chat.getPrimaryGroup(e.getPlayer())));
//                    prefixedUsers.add(e.getPlayer().getUniqueId());
//                } else plugin.getLogger().warning("Chat provider is null. Prefixes will not be added.");
//            }
//        }
//    }
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerJoin_patreonIntg(PlayerJoinEvent e) {
//        if (appReg == null) return;
//        var reg = appReg.getProvider();
//        var patreon = reg.getApplication(PatreonApplication.class);
//        var chat = rsp == null ? null : rsp.getProvider();
//        var player = e.getPlayer();
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//            int cached = patreon.getPledgingAmountCached(e.getPlayer().getUniqueId());
//            if (cached == 0) {
//                cached = patreon.getPledgingAmountSync(player.getUniqueId(), patreon.getConnection(player.getUniqueId()));
//            }
//            if (cached >= patreon.getRequiredPledge()) {
//                plugin.getLogger().info("Setting prefix of player " + player.getName());
//                if (chat != null) {
//                    var currentPrefix = chat.getPlayerPrefix(player) == null ? "" : chat.getPlayerPrefix(player);
//                    if (currentPrefix.contains("[$]")) {
//                        plugin.getLogger().info("Prefix is already set for player");
//                        return;
//                    }
//                    patreonUsersPrefixes.put(player.getUniqueId(), currentPrefix);
//                    chat.setPlayerPrefix(player, "&3[$] " + currentPrefix + chat.getGroupPrefix(player.getWorld(), chat.getPrimaryGroup(player)));
//                }
//            } else if (cached == -1) {
//                plugin.getLogger().info("Prefix not set for player " + player.getName() + " because they are not a patron or are not linked with Patreon.");
//            } else {
//                plugin.getLogger().info("Prefix not set for player " + player.getName() + " because their pledge is less than " + patreon.getRequiredPledge() + " cents. [" + cached + "]");
//            }
//        });
//    }
//
//    @EventHandler(priority = EventPriority.HIGH)
//    public void onPlayerJoin_discordIntg(PlayerLoginEvent e) {
//        if (appReg == null) return;
//        boolean whitelist = Bukkit.hasWhitelist();
//        var reg = appReg.getProvider();
//        var discord = reg.getApplication(DiscordApplication.class);
//        var id = e.getPlayer().getUniqueId();
//
//        var user = discord.getConnection(id);
//        if (whitelist && Bukkit.getServer().getWhitelistedPlayers().stream().anyMatch(op -> op.getUniqueId().equals(id))) {
//            if (e.getPlayer().hasPermission("greenfieldcore.discord.exempt")) {
//                Bukkit.getLogger().info("User " + e.getPlayer().getName() + " is exempted from having a linked discord profile.");
//                return;
//            }
//            if (user == null) e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.notConnected", "null"));
//            else if (user.isExpired()) {
//                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.expiredUser", "null"));
//                discord.removeConnection(id);
//            } else if (discord.isRefreshingUserToken(id)) {
//                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.refreshingUserToken", "null"));
//            } else if (discord.isGettingDiscordUserProfile(id)) {
//                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, discord.getAppConfig().getString("messages.gettingDiscordProfile", "null"));
//            } else if (user.isAlmostExpired()) {
//                Bukkit.getLogger().info(user.getTimeUntilExpiration());
//                discord.refreshUserToken(id, user);
//            } else discord.getDiscordUserAsync(id, user);
//            return;
//        }
//
//        //any moment after this means either the user is not whitelisted (meaning they arent required to have a discord account)
//        //or the whitelist is disabled. in which case if they have an account associated with their account, we update it, if not,
//        //not a big deal, we just remove the connection
//        if (user == null) return;
//
//        if (user.isExpired()) {
//            e.getPlayer().sendMessage(discord.getAppConfig().getString("messages.expiredUser", "null"));
//            discord.removeConnection(id);
//        }
//
//        if (user.isAlmostExpired()) {
//            Bukkit.getLogger().info(user.getTimeUntilExpiration());
//            discord.refreshUserToken(id, user);
//        }
//
//    }



}
