package net.greenfieldmc.core.authhub.services;

import com.njdaeger.authenticationhub.AuthhubLoginEvent;
import com.njdaeger.authenticationhub.ConnectionRequirement;
import com.njdaeger.authenticationhub.discord.DiscordUserLoginEvent;
import com.njdaeger.authenticationhub.patreon.PatreonUserLoginEvent;
import net.greenfieldmc.core.ComponentUtils;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import io.papermc.paper.ban.BanListType;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import net.greenfieldmc.core.greenfieldapi.models.GfDiscordConnection;
import net.greenfieldmc.core.greenfieldapi.models.GfPatreonConnection;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldCoreApi;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthhubIntegrationService extends ModuleService<AuthhubIntegrationService> implements IModuleService<AuthhubIntegrationService>, Listener {

    private final List<UUID> resolvingDiscordConnections = new ArrayList<>();
    private final List<UUID> resolvingPatreonConnections = new ArrayList<>();

    private final Map<UUID, List<GfPatreonConnection>> patreonPledgeCache = new HashMap<>();
    private final Map<UUID, List<GfDiscordConnection>> discordConnectionCache = new HashMap<>();
    private final Map<UUID, Long> patreonLastCheckTime = new HashMap<>();

    private final IGreenfieldCoreApi greenfieldCoreApi;
    private final IAuthhubService authhubService;
    private final List<UUID> prefixedUsers = new ArrayList<>();

    private static final int PREFIX_PRIORITY = 3;
    private static final long PATREON_CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    public AuthhubIntegrationService(Plugin plugin, Module module, IAuthhubService authhubService, IGreenfieldCoreApi greenfieldCoreApi) {
        super(plugin, module);
        this.authhubService = authhubService;
        this.greenfieldCoreApi = greenfieldCoreApi;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        var authhubPlugin = plugin.getServer().getPluginManager().getPlugin("AuthenticationHub");
        if (authhubPlugin == null) {
            throw new Exception("AuthenticationHub not found");
        }

//        createConnectionRequirement();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        // Remove prefixes for all users that had them set
//        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
//            for (UUID uuid : prefixedUsers) {
//                var currentPrefix = vaultService.getUserPrefix(uuid).join();
//                if (currentPrefix != null && currentPrefix.contains("&3[$]")) {
//                    var newPfx = currentPrefix.replace("&3[$]", "").trim();
//                    if (newPfx.isEmpty()) newPfx = null;
//                    var result = vaultService.setUserPrefix(uuid, newPfx).join();
//                    if (!result) getModule().getLogger().warning("Failed to remove prefix for player with UUID " + uuid);
//                }
//            }
//        });

        prefixedUsers.forEach(uuid -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " meta removeprefix " + PREFIX_PRIORITY);
        });
    }

    @EventHandler
    public void onDiscordLogin(AuthhubLoginEvent ale) {

        var player = ale.getPlayer();
        var cached = discordConnectionCache.get(player.getUniqueId());

        if (resolvingDiscordConnections.contains(player.getUniqueId())) {
            getModule().getLogger().info("Already resolving discord connections for user " + player.getName() + ".");
            return;
        }

        if (cached != null && !cached.isEmpty()) {
            getModule().getLogger().info("Found cached discord connections for user " + player.getName() + ", skipping lookup.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            getModule().getLogger().info("Resolving discord connections for user " + player.getName() + "...");
            resolvingDiscordConnections.add(player.getUniqueId());
            var foundUsers = resolveDiscordConnections(player.getUniqueId(), 0, 4, 250);
            if (foundUsers == null) {
                getModule().getLogger().warning("Failed to resolve discord connections for user " + player.getName() + " after multiple attempts.");
            } else if (foundUsers.isEmpty()) {
                getModule().getLogger().info("No linked discord accounts found for user " + player.getName() + ".");
            } else {
                getModule().getLogger().info("Found " + foundUsers.size() + " linked discord account(s) for user " + player.getName() + ".");
                discordConnectionCache.put(player.getUniqueId(), foundUsers);
            }
            resolvingDiscordConnections.remove(player.getUniqueId());

            if (foundUsers == null || foundUsers.isEmpty()) {
                var pbl = Bukkit.getBanList(BanListType.PROFILE);
                if (player.isWhitelisted() && !pbl.isBanned(player.getPlayerProfile()) && !player.hasPermission("greenfieldcore.discord.exempt")) {
                    getModule().getLogger().info("User " + player.getName() + " must have a linked discord profile.");
                    var connectionLinkResult = greenfieldCoreApi.getDiscordConnectionLink(greenfieldCoreApi.getUserByMinecraftUuid(player.getUniqueId()).join().getData().getUserId()).join();
                    if (connectionLinkResult.isFailure()) {
                        getModule().getLogger().warning("Failed to retrieve discord connection link for user " + player.getName() + ". Error: " + connectionLinkResult.getErrorMessage());
                        return;
                    }
                    Bukkit.getScheduler().runTask(getPlugin(), () -> {
                        if (!player.isOnline()) return;
                        player.sendMessage(ComponentUtils.moduleMessage("GreenfieldCore", "Hey " + player.getName() + "! It looks like you don't have a linked Discord account, which is required (and will soon be enforced) to join the server. Please click the link below to link your account.")
                                .append(Component.newline())
                                .append(Component.text("[CONNECT]", ChatFormatModule.linkStyle.apply(connectionLinkResult.getData()))));
                    });
                } else {
                    getModule().getLogger().info("User " + player.getName() + " does not need a linked discord profile - they were not found in the whitelist or they are a banned member.");
                }
            }
        });
    }

    @EventHandler
    public void onPatreonLogin(AuthhubLoginEvent ale) {
        var player = ale.getPlayer();

        // Whitelisted players bypass Patreon requirements
        if (player.isWhitelisted()) {
            getModule().getLogger().info("User " + player.getName() + " is whitelisted, bypassing Patreon check.");
            return;
        }

        // Check if already resolving
        if (resolvingPatreonConnections.contains(player.getUniqueId())) {
            getModule().getLogger().info("Already resolving patreon connections for user " + player.getName() + ".");
            ale.disallow("Your Patreon pledge status is currently being verified. Please wait a moment and try again.");
            return;
        }

        var cached = patreonPledgeCache.get(player.getUniqueId());
        var lastCheckTime = patreonLastCheckTime.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        // Check if we have a recent check (within 5 minutes)
        if (lastCheckTime != null && (currentTime - lastCheckTime) < PATREON_CACHE_DURATION_MS) {
            // We checked recently, use cached result without hitting API
            int requiredPledge = authhubService.getRequiredPatreonPledge();
            if (cached == null || cached.isEmpty()) {
                getModule().getLogger().info("User " + player.getName() + " has no Patreon connections (cached check within 5 minutes), denying login.");
                ale.disallow("You must be Architect patreon or a build member to join the server.");
                return;
            }

            boolean hasValidPledge = cached.stream()
                    .anyMatch(conn -> conn.getPledge() >= requiredPledge);

            if (!hasValidPledge) {
                getModule().getLogger().info("User " + player.getName() + " does not have sufficient Patreon pledge (cached check within 5 minutes), denying login.");
                ale.disallow("You must be Architect patreon or a build member to join the server.");
                return;
            }

            getModule().getLogger().info("User " + player.getName() + " has sufficient cached Patreon pledge (checked within 5 minutes).");
            ale.allow();
            setPatreonPrefix(player);
            return;
        }

        // If no cache, initiate async resolution
        if (cached == null || cached.isEmpty()) {
            getModule().getLogger().info("No cached patreon connections for user " + player.getName() + ", initiating lookup...");
            ale.disallow("Your Patreon pledge status is currently being verified. Please wait a moment and try again.");
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                getModule().getLogger().info("Resolving patreon connections for user " + player.getName() + "...");
                resolvingPatreonConnections.add(player.getUniqueId());
                var foundConnections = resolvePatreonConnections(player.getUniqueId(), 0, 4, 250);

                // Update timestamp
                patreonLastCheckTime.put(player.getUniqueId(), System.currentTimeMillis());

                if (foundConnections == null) {
                    getModule().getLogger().warning("Failed to resolve patreon connections for user " + player.getName() + " after multiple attempts.");
                } else if (foundConnections.isEmpty()) {
                    getModule().getLogger().info("No linked patreon accounts found for user " + player.getName() + ".");
                } else {
                    getModule().getLogger().info("Found " + foundConnections.size() + " linked patreon account(s) for user " + player.getName() + ".");
                }
                resolvingPatreonConnections.remove(player.getUniqueId());

                // Check if any connection has sufficient pledge
                if (foundConnections != null && !foundConnections.isEmpty()) {
                    boolean hasValidPledge = foundConnections.stream()
                            .anyMatch(conn -> conn.getPledge() >= authhubService.getRequiredPatreonPledge());

                    if (!hasValidPledge) {
                        getModule().getLogger().info("User " + player.getName() + " does not have sufficient Patreon pledge, denying login.");
                    } else {
                        getModule().getLogger().info("User " + player.getName() + " has sufficient Patreon pledge.");
                    }
                } else {
                    // No connections found, deny login
                    getModule().getLogger().info("User " + player.getName() + " has no Patreon connections, denying login.");
                }
            });
            return;
        }

        // Check cached pledges for sufficient amount
        int requiredPledge = authhubService.getRequiredPatreonPledge();
        boolean hasValidPledge = cached.stream()
                .anyMatch(conn -> conn.getPledge() >= requiredPledge);

        if (hasValidPledge) {
            getModule().getLogger().info("User " + player.getName() + " has sufficient cached Patreon pledge.");
            ale.allow();
            setPatreonPrefix(player);
            return;
        }

        // No valid pledge in cache, attempt to refresh connections
        getModule().getLogger().info("User " + player.getName() + " does not have sufficient cached pledge, refreshing connections...");
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<GfPatreonConnection> refreshedConnections = new ArrayList<>();

            for (GfPatreonConnection connection : cached) {
                var refreshResult = greenfieldCoreApi.refreshPatreonConnection(connection.getPatreonConnectionId()).join();
                if (refreshResult.isSuccess()) {
                    refreshedConnections.add(refreshResult.getData());
                    getModule().getLogger().info("Refreshed patreon connection " + connection.getPatreonConnectionId() + " for user " + player.getName());
                } else {
                    getModule().getLogger().warning("Failed to refresh patreon connection " + connection.getPatreonConnectionId() + " for user " + player.getName() + ": " + refreshResult.getErrorMessage());
                    // Keep the old connection data
                    refreshedConnections.add(connection);
                }
            }

            // Update cache with refreshed data and timestamp
            patreonPledgeCache.put(player.getUniqueId(), refreshedConnections);
            patreonLastCheckTime.put(player.getUniqueId(), System.currentTimeMillis());

            // Check if any refreshed connection has sufficient pledge
            boolean hasValidRefreshedPledge = refreshedConnections.stream()
                    .anyMatch(conn -> conn.getPledge() >= requiredPledge);

            if (hasValidRefreshedPledge) {
                getModule().getLogger().info("User " + player.getName() + " has sufficient Patreon pledge after refresh.");
            } else {
                getModule().getLogger().info("User " + player.getName() + " does not have sufficient Patreon pledge after refresh, denying login.");
            }
        });
    }

    private List<GfDiscordConnection> resolveDiscordConnections(UUID minecraftUuid, int attempt, int maxAttempts, long waitInMillis) {
        if (attempt >= 1) {
            try {
                Thread.sleep(waitInMillis * attempt);
            } catch (InterruptedException e) {
                getModule().getLogger().warning("Interrupted while waiting to retry resolving discord connections for user with UUID " + minecraftUuid);
                Thread.currentThread().interrupt();
                return null;
            }
        }
        if (attempt > maxAttempts) {
            getModule().getLogger().warning("Max attempts reached while trying to resolve discord connections for user with UUID " + minecraftUuid);
            return null;
        }
        var userResult = greenfieldCoreApi.getUserByMinecraftUuid(minecraftUuid).join();
        if (userResult.isFailure()) {
            getModule().getLogger().warning("Failed to retrieve user for UUID " + minecraftUuid + " while resolving discord connections. Attempt " + attempt + " of " + maxAttempts);
            return resolveDiscordConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        var accountResult = greenfieldCoreApi.getDiscordConnection(userResult.getData().getUserId()).join();
        if (accountResult.isFailure()) {
            getModule().getLogger().warning("Failed to retrieve discord connections for user with UUID " + minecraftUuid + ". Attempt " + attempt + " of " + maxAttempts);
            return resolveDiscordConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        return List.of(accountResult.getData());
    }

    private List<GfPatreonConnection> resolvePatreonConnections(UUID minecraftUuid, int attempt, int maxAttempts, long waitMillis) {
        if (attempt >= 1) {
            try {
                Thread.sleep(waitMillis * attempt);
            } catch (InterruptedException e) {
                getModule().getLogger().warning("Interrupted while waiting to retry resolving patreon connections for user with UUID " + minecraftUuid);
                Thread.currentThread().interrupt();
                return null;
            }
        }
        if (attempt > maxAttempts) {
            getModule().getLogger().warning("Max attempts reached while trying to resolve patreon connections for user with UUID " + minecraftUuid);
            return null;
        }
        var userResult = greenfieldCoreApi.getUserByMinecraftUuid(minecraftUuid).join();
        if (userResult.isFailure()) {
            getModule().getLogger().warning("Failed to retrieve user for UUID " + minecraftUuid + " while resolving discord connections. Attempt " + attempt + " of " + maxAttempts);
            return resolvePatreonConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        var accountResult = greenfieldCoreApi.getPatreonConnection(userResult.getData().getUserId()).join();
        if (accountResult.isFailure()) {
            getModule().getLogger().warning("Failed to retrieve discord connections for user with UUID " + minecraftUuid + ". Attempt " + attempt + " of " + maxAttempts);
            return resolvePatreonConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        return List.of(accountResult.getData());
    }

    private void setPatreonPrefix(Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getUniqueId() + " meta setprefix " + PREFIX_PRIORITY + " \"&3[$] \"");
        prefixedUsers.add(player.getUniqueId());
    }


//    @EventHandler
//    public void onDiscordLogin(DiscordUserLoginEvent e) {
//        if (!e.getPlayer().isBanned()) e.allow();
//    }
//
//    @EventHandler
//    public void onPatreonLogin(PatreonUserLoginEvent e) {
//        if (e.getUser().getPledgingAmount() < authhubService.getRequiredPatreonPledge() && e.getApplication().getConnectionRequirement().isRequired(e.getPlayer())) {
//            e.disallow("Your patron account currently pledges " + e.getUser().getPledgingAmount() + " cents, which is less than the required " + authhubService.getRequiredPatreonPledge() + " cents. Please upgrade your patronage to continue.");
//        } else {
//            e.allow();
//            //going to just use raw LP commands instead of trying to use their api
//
//            if (e.getUser().getPledgingAmount() < authhubService.getRequiredPatreonPledge()) {
//                getModule().getLogger().info("User " + e.getPlayer().getName() + " is not a patron, skipping prefix setting.");
//                return;
//            }
//
//            if (prefixedUsers.contains(e.getPlayer().getUniqueId())) return;
//
//            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + e.getPlayer().getUniqueId() + " meta setprefix " + PREFIX_PRIORITY + " \"&3[$] \"");
//            prefixedUsers.add(e.getPlayer().getUniqueId());
//
////            if (vaultService.isEnabled()) {
////                Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
////                    if (e.getUser().getPledgingAmount() < authhubService.getRequiredPatreonPledge()) {
////                        getModule().getLogger().info("User " + e.getPlayer().getName() + " is not a patron, skipping prefix setting.");
////                        return;
////                    }
////                    var prefix = vaultService.getUserPrefix(e.getPlayer().getUniqueId()).join();
////                    var currentPrefix = prefix == null ? "" : prefix;
////                    if (currentPrefix.contains("&3[$]")) {
////                        if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) prefixedUsers.add(e.getPlayer().getUniqueId());
////                        getModule().getLogger().info("Prefix is " + currentPrefix + " for player " + e.getPlayer().getName());
////                        return;
////                    }
////                    var newPfx = currentPrefix.isEmpty() ? "&3[$]" : "&3[$] " + currentPrefix;
////                    var result = vaultService.setUserPrefix(e.getPlayer().getUniqueId(), newPfx).join();
////                    if (!result)
////                        getModule().getLogger().warning("Failed to set prefix for player " + e.getPlayer().getName());
////                    else if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) prefixedUsers.add(e.getPlayer().getUniqueId());
////                });
////            } else getModule().getLogger().warning("Vault service is not enabled, prefixes will not be set for Patrons.");
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) return;
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + e.getPlayer().getUniqueId() + " meta removeprefix " + PREFIX_PRIORITY);
        prefixedUsers.remove(e.getPlayer().getUniqueId());
//        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
//            var currentPrefix = vaultService.getUserPrefix(e.getPlayer().getUniqueId()).join();
//            if (currentPrefix != null && currentPrefix.contains("&3[$]")) {
//                var newPfx = currentPrefix.replace("&3[$]", "").trim();
//                if (newPfx.isEmpty()) newPfx = null;
//                var result = vaultService.setUserPrefix(e.getPlayer().getUniqueId(), newPfx).join();
//                if (!result) getModule().getLogger().warning("Failed to remove prefix for player " + e.getPlayer().getName());
//            }
//        });
    }

//    private void createConnectionRequirement() {
//        var pbl = Bukkit.getBanList(BanListType.PROFILE);
//        new ConnectionRequirement("DISCORD_REQUIREMENT", (p) -> {
//            if (p.hasPermission("greenfieldcore.discord.exempt")) {
//                getModule().getLogger().info("User " + p.getName() + " is exempted from having a linked discord profile.");
//                return false;
//            } else if (p.isWhitelisted() && !pbl.isBanned(p.getPlayerProfile())) {
//                getModule().getLogger().info("User " + p.getName() + " must have a linked discord profile.");
//                return true;
//            }
//            getModule().getLogger().info("User " + p.getName() + " does not need a linked discord profile - they were not found in the whitelist or they are a banned member.");
//            return false;
//        });
//    }

}
