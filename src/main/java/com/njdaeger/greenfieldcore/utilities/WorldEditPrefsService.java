package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.SideEffect;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldEditPrefsService extends ModuleService<WorldEditPrefsService> implements IModuleService<WorldEditPrefsService>, Listener {

    private final Set<UUID> overriddenPerfs = new HashSet<>();
    private WorldEditPlugin worldEditPlugin;

    public WorldEditPrefsService(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } else {
            throw new Exception("WorldEdit is not installed.");
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

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
