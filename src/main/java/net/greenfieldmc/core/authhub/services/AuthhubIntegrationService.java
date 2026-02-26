package net.greenfieldmc.core.authhub.services;

import com.njdaeger.authenticationhub.AuthhubLoginEvent;
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

    private final IGreenfieldCoreApi greenfieldCoreApi;
    private final IAuthhubService authhubService;
    private final List<UUID> prefixedUsers = new ArrayList<>();

    private static final int PREFIX_PRIORITY = 3;

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

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        prefixedUsers.forEach(uuid -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " meta removeprefix " + PREFIX_PRIORITY));
    }

    @EventHandler
    public void onDiscordLogin(AuthhubLoginEvent ale) {

        var player = ale.getPlayer();
        var pbl = Bukkit.getBanList(BanListType.PROFILE);
        if (player.hasPermission("greenfieldcore.discord.exempt") || !player.isWhitelisted() || pbl.isBanned(player.getPlayerProfile())) {
            getModule().getLogger().info("User " + player.getName() + " is exempted from having a linked discord profile or does not need one.");
            return;
        }

        if (resolvingDiscordConnections.contains(player.getUniqueId())) {
            getModule().getLogger().info("Already resolving discord connections for user " + player.getName() + ".");
            return;
        }

        var cached = discordConnectionCache.get(player.getUniqueId());
        if (cached != null && !cached.isEmpty()) {
            getModule().getLogger().info("Found cached discord connections for user " + player.getName() + ", skipping lookup.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            getModule().getLogger().info("Resolving discord connections for user " + player.getName() + "...");
            var foundUsers = resolveDiscordConnections(player.getUniqueId(), 0, 4, 250);

            if (foundUsers == null) {
                getModule().getLogger().warning("Failed to resolve discord connections for user " + player.getName() + " after multiple attempts.");
                return;
            }

            discordConnectionCache.put(player.getUniqueId(), foundUsers);
            if (!foundUsers.isEmpty()) {
                getModule().getLogger().info("Found " + foundUsers.size() + " linked discord account(s) for user " + player.getName() + ".");
                return;
            }

            getModule().getLogger().info("User " + player.getName() + " does not have a linked discord account, sending connection link.");
            greenfieldCoreApi.getDiscordConnectionLink(greenfieldCoreApi.getUserByMinecraftUuid(player.getUniqueId()).join().getData().getUserId()).join()
                    .ifFailure(errorMsg -> getModule().getLogger().warning("Failed to retrieve user for UUID " + player.getUniqueId() + " while getting discord connection link: " + errorMsg))
                    .ifSuccess(connectionLink -> Bukkit.getScheduler().runTask(getPlugin(), () -> {
                        if (!player.isOnline()) return;
                        player.sendMessage(ComponentUtils.moduleMessage("GreenfieldCore", "Hey " + player.getName() + "! It looks like you don't have a linked Discord account, which is required (and will soon be enforced) to join the server. Please click the link below to link your account.")
                                .append(Component.newline())
                                .append(Component.text("[CONNECT]", ChatFormatModule.linkStyle.apply(connectionLink))));
                    })
            );
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

        var cached = patreonPledgeCache.get(player.getUniqueId());
        if (cached != null && !cached.isEmpty() && cached.stream().anyMatch(conn -> conn.getPledge() >= authhubService.getRequiredPatreonPledge())) {
            getModule().getLogger().info("User " + player.getName() + " has a valid Patreon pledge in cache, allowing login.");
            ale.allow();
            setPatreonPrefix(player);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (resolvingDiscordConnections.contains(player.getUniqueId())) {
                getModule().getLogger().info("Already resolving patreon connections for user " + player.getName() + ".");
                return;
            }
            getModule().getLogger().info("Resolving patreon connections for user " + player.getName() + "...");
            var foundConnections = resolvePatreonConnections(player.getUniqueId(), 0, 4, 250);
            if (foundConnections == null) {
                getModule().getLogger().warning("Failed to resolve patreon connections for user " + player.getName() + " after multiple attempts.");
                return;
            }
            if (foundConnections.isEmpty()) {
                getModule().getLogger().info("No linked patreon accounts found for user " + player.getName() + ".");
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
        resolvingDiscordConnections.remove(minecraftUuid);
        return List.of(accountResult.getData());
    }

    private List<GfPatreonConnection> resolvePatreonConnections(UUID minecraftUuid, int attempt, int maxAttempts, long waitMillis) {
        if (!resolvingPatreonConnections.contains(minecraftUuid)) resolvingPatreonConnections.add(minecraftUuid);
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
