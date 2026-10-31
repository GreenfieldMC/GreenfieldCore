package net.greenfieldmc.core.authhub.services;

import com.njdaeger.authenticationhub.AuthhubLoginEvent;
import net.greenfieldmc.core.ComponentUtils;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import io.papermc.paper.ban.BanListType;
import net.greenfieldmc.core.chatformat.ChatFormatModule;
import net.greenfieldmc.core.greenfieldapi.models.users.GfDiscordConnection;
import net.greenfieldmc.core.greenfieldapi.models.users.GfPatreonConnection;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldUserApiService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AuthhubIntegrationService implements Listener, AutoCloseable {

    private final List<UUID> resolvingDiscordConnections = new ArrayList<>();
    private final List<UUID> resolvingPatreonConnections = new ArrayList<>();

    private final Map<UUID, List<GfPatreonConnection>> patreonPledgeCache = new HashMap<>();
    private final Map<UUID, List<GfDiscordConnection>> discordConnectionCache = new HashMap<>();

    private final IGreenfieldCorePlugin plugin;
    private final Logger logger;
    private final IGreenfieldUserApiService userApi;
    private final IAuthhubService authhubService;
    private final List<UUID> prefixedUsers = new ArrayList<>();

    private static final int PREFIX_PRIORITY = 3;

    public AuthhubIntegrationService(IGreenfieldCorePlugin plugin, Logger logger, IAuthhubService authhubService, IGreenfieldUserApiService userApi) throws Exception {
        if (plugin.getServer().getPluginManager().getPlugin("AuthenticationHub") == null) {
            throw new Exception("AuthenticationHub not found");
        }
        this.plugin = plugin;
        this.logger = logger;
        this.authhubService = authhubService;
        this.userApi = userApi;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void close() {
        prefixedUsers.forEach(uuid -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " meta removeprefix " + PREFIX_PRIORITY));
    }

    @EventHandler
    public void onDiscordLogin(AuthhubLoginEvent ale) {

        var player = ale.getPlayer();
        var pbl = Bukkit.getBanList(BanListType.PROFILE);
        if (player.hasPermission("greenfieldcore.discord.exempt") || !player.isWhitelisted() || pbl.isBanned(player.getPlayerProfile())) {
            logger.info("User " + player.getName() + " is exempted from having a linked discord profile or does not need one.");
            return;
        }

        if (resolvingDiscordConnections.contains(player.getUniqueId())) {
            logger.info("Already resolving discord connections for user " + player.getName() + ".");
            return;
        }

        var cached = discordConnectionCache.get(player.getUniqueId());
        if (cached != null && !cached.isEmpty()) {
            logger.info("Found cached discord connections for user " + player.getName() + ", skipping lookup.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            logger.info("Resolving discord connections for user " + player.getName() + "...");
            var foundUsers = resolveDiscordConnections(player.getUniqueId(), 0, 4, 250);

            if (foundUsers == null) {
                logger.warning("Failed to resolve discord connections for user " + player.getName() + " after multiple attempts.");
                return;
            }

            discordConnectionCache.put(player.getUniqueId(), foundUsers);
            if (!foundUsers.isEmpty()) {
                logger.info("Found " + foundUsers.size() + " linked discord account(s) for user " + player.getName() + ".");
                return;
            }

            logger.info("User " + player.getName() + " does not have a linked discord account, sending connection link.");
            var foundUser = userApi.getUserByMinecraftUuid(player.getUniqueId()).join().getData();
            userApi.getDiscordConnectionLink(foundUser.getUserId()).join()
                    .ifFailure(errorMsg -> logger.warning("Failed to retrieve user for UUID " + player.getUniqueId() + " while getting discord connection link: " + errorMsg))
                    .ifSuccess(connectionLink -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!player.isOnline()) {
                            logger.warning("Player " + player.getName() + " went offline before we could send the discord connection link.");
                            return;
                        }
                        player.sendMessage(ComponentUtils.moduleMessage("GreenfieldCore", "Hey " + player.getName() + "! It looks like you don't have a linked Discord account, which is required (and will soon be enforced) to join the server. Please click the link below to link your account.")
                                .append(Component.newline())
                                .append(Component.text("[CONNECT]", ChatFormatModule.linkStyle.apply(connectionLink))));
                    }, 40L)
                );
        });
    }

    @EventHandler
    public void onPatreonLogin(AuthhubLoginEvent ale) {
        var player = ale.getPlayer();

        // Whitelisted players bypass Patreon requirements
        if (player.isWhitelisted()) {
            logger.info("User " + player.getName() + " is whitelisted, bypassing Patreon check.");
            return;
        }

        var cached = patreonPledgeCache.get(player.getUniqueId());
        if (cached != null && !cached.isEmpty() && cached.stream().anyMatch(conn -> conn.getPledge() >= authhubService.getRequiredPatreonPledge())) {
            logger.info("User " + player.getName() + " has a valid Patreon pledge in cache, allowing login.");
            ale.allow();
            setPatreonPrefix(player);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (resolvingDiscordConnections.contains(player.getUniqueId())) {
                logger.info("Already resolving patreon connections for user " + player.getName() + ".");
                return;
            }
            logger.info("Resolving patreon connections for user " + player.getName() + "...");
            var foundConnections = resolvePatreonConnections(player.getUniqueId(), 0, 4, 250);
            if (foundConnections == null) {
                logger.warning("Failed to resolve patreon connections for user " + player.getName() + " after multiple attempts.");
                return;
            }
            if (foundConnections.isEmpty()) {
                logger.info("No linked patreon accounts found for user " + player.getName() + ".");
                return;
            }

            patreonPledgeCache.put(player.getUniqueId(), foundConnections);
        });

        if (resolvingPatreonConnections.contains(player.getUniqueId())) ale.disallow("Your Patreon pledge status is currently being verified. Please wait a moment and try again.");
        else ale.disallow("You must be an Architect patron or a build member to join the server. Your Patreon pledge status is currently being verified, please wait a moment and try again.");
    }

    private List<GfDiscordConnection> resolveDiscordConnections(UUID minecraftUuid, int attempt, int maxAttempts, long waitInMillis) {
        if (!resolvingDiscordConnections.contains(minecraftUuid)) resolvingDiscordConnections.add(minecraftUuid);
        if (attempt >= 1) {
            try {
                Thread.sleep(waitInMillis * attempt);
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting to retry resolving discord connections for user with UUID " + minecraftUuid);
                Thread.currentThread().interrupt();
                resolvingDiscordConnections.remove(minecraftUuid);
                return null;
            }
        }
        if (attempt > maxAttempts) {
            logger.warning("Max attempts reached while trying to resolve discord connections for user with UUID " + minecraftUuid);
            resolvingDiscordConnections.remove(minecraftUuid);
            return null;
        }
        var userResult = userApi.getUserByMinecraftUuid(minecraftUuid).join();
        if (userResult.isFailure()) {
            logger.warning("Failed to retrieve user for UUID " + minecraftUuid + " while resolving discord connections. Attempt " + attempt + " of " + maxAttempts);
            return resolveDiscordConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        var accountResult = userApi.getDiscordConnection(userResult.getData().getUserId()).join();
        if (accountResult.isFailure()) {
            logger.warning("Failed to retrieve discord connections for user with UUID " + minecraftUuid + ". Attempt " + attempt + " of " + maxAttempts);
            return resolveDiscordConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        resolvingDiscordConnections.remove(minecraftUuid);
        return List.of(accountResult.getData());
    }

    private List<GfPatreonConnection> resolvePatreonConnections(UUID minecraftUuid, int attempt, int maxAttempts, long waitMillis) {
        if (!resolvingPatreonConnections.contains(minecraftUuid)) resolvingPatreonConnections.add(minecraftUuid);
        if (attempt >= 1) {
            try {
                Thread.sleep(waitMillis * attempt);
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting to retry resolving patreon connections for user with UUID " + minecraftUuid);
                Thread.currentThread().interrupt();
                resolvingPatreonConnections.remove(minecraftUuid);
                return null;
            }
        }
        if (attempt > maxAttempts) {
            logger.warning("Max attempts reached while trying to resolve patreon connections for user with UUID " + minecraftUuid);
            resolvingPatreonConnections.remove(minecraftUuid);
            return null;
        }
        var userResult = userApi.getUserByMinecraftUuid(minecraftUuid).join();
        if (userResult.isFailure()) {
            logger.warning("Failed to retrieve user for UUID " + minecraftUuid + " while resolving discord connections. Attempt " + attempt + " of " + maxAttempts);
            return resolvePatreonConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        var accountResult = userApi.getPatreonConnection(userResult.getData().getUserId()).join();
        if (accountResult.isFailure()) {
            logger.warning("Failed to retrieve discord connections for user with UUID " + minecraftUuid + ". Attempt " + attempt + " of " + maxAttempts);
            return resolvePatreonConnections(minecraftUuid, attempt + 1, maxAttempts, 250);
        }
        resolvingPatreonConnections.remove(minecraftUuid);
        return List.of(accountResult.getData());
    }

    private void setPatreonPrefix(Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getUniqueId() + " meta setprefix " + PREFIX_PRIORITY + " \"&3[$] \"");
        prefixedUsers.add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) return;
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + e.getPlayer().getUniqueId() + " meta removeprefix " + PREFIX_PRIORITY);
        prefixedUsers.remove(e.getPlayer().getUniqueId());
    }


}
