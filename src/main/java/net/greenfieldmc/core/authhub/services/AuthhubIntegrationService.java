package net.greenfieldmc.core.authhub.services;

import com.njdaeger.authenticationhub.ConnectionRequirement;
import com.njdaeger.authenticationhub.discord.DiscordUserLoginEvent;
import com.njdaeger.authenticationhub.patreon.PatreonUserLoginEvent;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.shared.services.IVaultService;
import io.papermc.paper.ban.BanListType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthhubIntegrationService extends ModuleService<AuthhubIntegrationService> implements IModuleService<AuthhubIntegrationService>, Listener {

    private final IVaultService vaultService;
    private final IAuthhubService authhubService;
    private final List<UUID> prefixedUsers = new ArrayList<>();

    public AuthhubIntegrationService(Plugin plugin, Module module, IVaultService vaultService, IAuthhubService authhubService) {
        super(plugin, module);
        this.vaultService = vaultService;
        this.authhubService = authhubService;
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

        createConnectionRequirement();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        if (!vaultService.isEnabled()) return;
        // Remove prefixes for all users that had them set
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            for (UUID uuid : prefixedUsers) {
                var currentPrefix = vaultService.getUserPrefix(uuid).join();
                if (currentPrefix != null && currentPrefix.contains("&3[$]")) {
                    var newPfx = currentPrefix.replace("&3[$]", "").trim();
                    if (newPfx.isEmpty()) newPfx = null;
                    var result = vaultService.setUserPrefix(uuid, newPfx).join();
                    if (!result) getModule().getLogger().warning("Failed to remove prefix for player with UUID " + uuid);
                }
            }
        });
    }

    @EventHandler
    public void onDiscordLogin(DiscordUserLoginEvent e) {
        if (!e.getPlayer().isBanned()) e.allow();
    }

    @EventHandler
    public void onPatreonLogin(PatreonUserLoginEvent e) {
        if (e.getUser().getPledgingAmount() < authhubService.getRequiredPatreonPledge() && e.getApplication().getConnectionRequirement().isRequired(e.getPlayer())) {
            e.disallow("Your patron account currently pledges " + e.getUser().getPledgingAmount() + " cents, which is less than the required " + authhubService.getRequiredPatreonPledge() + " cents. Please upgrade your patronage to continue.");
        } else {
            e.allow();
            if (vaultService.isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                    var prefix = vaultService.getUserPrefix(e.getPlayer().getUniqueId()).join();
                    var currentPrefix = prefix == null ? "" : prefix;
                    if (currentPrefix.contains("&3[$]")) {
                        if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) prefixedUsers.add(e.getPlayer().getUniqueId());
                        getModule().getLogger().info("Prefix is " + currentPrefix + " for player " + e.getPlayer().getName());
                        return;
                    }
                    var newPfx = currentPrefix.isEmpty() ? "&3[$]" : "&3[$] " + currentPrefix;
                    var result = vaultService.setUserPrefix(e.getPlayer().getUniqueId(), newPfx).join();
                    if (!result)
                        getModule().getLogger().warning("Failed to set prefix for player " + e.getPlayer().getName());
                    else if (!prefixedUsers.contains(e.getPlayer().getUniqueId())) prefixedUsers.add(e.getPlayer().getUniqueId());
                });
            } else getModule().getLogger().warning("Vault service is not enabled, prefixes will not be set for Patrons.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e) {
        if (!vaultService.isEnabled() || prefixedUsers.contains(e.getPlayer().getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            var currentPrefix = vaultService.getUserPrefix(e.getPlayer().getUniqueId()).join();
            if (currentPrefix != null && currentPrefix.contains("&3[$]")) {
                var newPfx = currentPrefix.replace("&3[$]", "").trim();
                if (newPfx.isEmpty()) newPfx = null;
                var result = vaultService.setUserPrefix(e.getPlayer().getUniqueId(), newPfx).join();
                if (!result) getModule().getLogger().warning("Failed to remove prefix for player " + e.getPlayer().getName());
            }
        });
    }

    private void createConnectionRequirement() {
        var pbl = Bukkit.getBanList(BanListType.PROFILE);
        new ConnectionRequirement("DISCORD_REQUIREMENT", (p) -> {
            if (p.hasPermission("greenfieldcore.discord.exempt")) {
                getModule().getLogger().info("User " + p.getName() + " is exempted from having a linked discord profile.");
                return false;
            } else if (p.isWhitelisted() && !pbl.isBanned(p.getPlayerProfile())) {
                getModule().getLogger().info("User " + p.getName() + " must have a linked discord profile.");
                return true;
            }
            getModule().getLogger().info("User " + p.getName() + " does not need a linked discord profile - they were not found in the whitelist or they are a banned member.");
            return false;
        });
    }

}
