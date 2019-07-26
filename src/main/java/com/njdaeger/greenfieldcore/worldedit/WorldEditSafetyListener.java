package com.njdaeger.greenfieldcore.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditSafetyListener implements Listener {

    private final WorldEditPlugin worldEdit;
    private final Map<UUID, String> commandHold;

    public WorldEditSafetyListener(WorldEditPlugin worldEdit) {
        this.worldEdit = worldEdit;
        this.commandHold = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void commandListener(PlayerCommandPreprocessEvent event) {
        if (WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getCommandManager().containsCommand(event.getMessage().substring(0, event.getMessage().indexOf(" ")))) {
            LocalSession session = worldEdit.getSession(event.getPlayer());

            if (session == null) return;

            /*
            TODO:
            - Check if the command ran is a command which doesnt rely on the selection
            - Store a snapshot of the users location and other possible information so when they move the command is sent from their stored location
             */

            try {
                Region region = session.getSelection(session.getSelectionWorld());
                int vol = region.getArea();

                if (vol > 1000) {
                    event.getPlayer().sendMessage("You sure dog?");
                }
            } catch (IncompleteRegionException e) {
                e.printStackTrace();
            }
        }
    }
}
