package net.greenfieldmc.core.utilities.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.SideEffect;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldEditPrefsService implements Listener {

    private final Set<UUID> overriddenPerfs = new HashSet<>();
    private final WorldEditPlugin worldEditPlugin;

    public WorldEditPrefsService(IGreenfieldCorePlugin plugin) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null)
            throw new Exception("WorldEdit is not installed.");
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().toLowerCase().startsWith("//perf")) overriddenPerfs.add(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (overriddenPerfs.contains(e.getPlayer().getUniqueId())) return;
        var localSession = worldEditPlugin.getSession(e.getPlayer());
        localSession.setSideEffectSet(localSession.getSideEffectSet()
                .with(SideEffect.VALIDATION, SideEffect.State.OFF)
                .with(SideEffect.UPDATE, SideEffect.State.OFF)
                .with(SideEffect.NEIGHBORS, SideEffect.State.OFF));
    }

}
