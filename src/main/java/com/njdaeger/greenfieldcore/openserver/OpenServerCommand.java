package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import org.bukkit.ChatColor;

import static com.njdaeger.greenfieldcore.Util.broadcast;

public class OpenServerCommand {

    private final OpenServerModule openServer;

    OpenServerCommand(GreenfieldCore plugin) {
        plugin.registerCommand(BCIBuilder.create("openserver")
                .executor(this::openServer)
                .permissions("greenfieldcore.openserver.command")
                .maxArgs(0)
                .usage("/openserver")
                .description("Turns the server into OpenServer mode.")
                .aliases("oserver", "os")
                .build());
        this.openServer = plugin.getOpenServerModule();
    }

    private void openServer(CommandContext context) {
        if (openServer.isEnabled()) {
            broadcast(ChatColor.LIGHT_PURPLE + "[OpenServer] OpenServer is now disabled.", "greenfieldcore.openserver.message");
            openServer.setEnabled(false);
        } else {
            broadcast(ChatColor.LIGHT_PURPLE + "[OpenServer] OpenServer is now enabled.", "greenfieldcore.openserver.message");
            openServer.setEnabled(true);
        }
    }

}
