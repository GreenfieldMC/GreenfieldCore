package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class UtilitiesModule extends Module implements Listener {

    private boolean worldeditPerfSet = false;
    private final Set<UUID> overriddenPerfs = new HashSet<>();

    public UtilitiesModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        new UtilityCommands(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if (Bukkit.getPluginManager().getPlugin("worldedit") != null) worldeditPerfSet = true;
        else plugin.getLogger().info("WorldEdit was not found. Perf settings will not be automatically set.");
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().toLowerCase().startsWith("//perf")) overriddenPerfs.add(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!worldeditPerfSet || overriddenPerfs.contains(e.getPlayer().getUniqueId())) return;
        var localSession = WorldEditPlugin.getPlugin(WorldEditPlugin.class).getSession(e.getPlayer());
        localSession.setSideEffectSet(localSession.getSideEffectSet()
                .with(SideEffect.VALIDATION, SideEffect.State.OFF)
                .with(SideEffect.UPDATE, SideEffect.State.OFF)
                .with(SideEffect.NEIGHBORS, SideEffect.State.OFF));
    }

}
