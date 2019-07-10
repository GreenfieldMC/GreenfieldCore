package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import org.bukkit.ChatColor;

import static com.njdaeger.greenfieldcore.Util.broadcast;
import static org.bukkit.ChatColor.*;

public class OpenServerCommand {

    private final OpenServerModule openServer;

    OpenServerCommand(GreenfieldCore plugin, OpenServerModule module) {
        plugin.registerCommand(BCIBuilder.create("openserver")
                .executor(this::openServer)
                .completer(this::completion)
                .permissions("greenfieldcore.openserver.command")
                .maxArgs(1)
                .usage("/openserver [reload|status]")
                .description("Turns the server into OpenServer mode.")
                .aliases("oserver", "os", "lock", "unlock")
                .build());
        this.openServer = module;
    }

    private void openServer(CommandContext context) throws BCIException {
        if (context.subCommand((ctx) -> ctx.getAlias().equalsIgnoreCase("lock"), this::lock)) return;
        if (context.subCommand((ctx) -> ctx.getAlias().equalsIgnoreCase("unlock"), this::unlock)) return;
        if (context.subCommandAt(0, "reload", true, (ctx) -> {
            context.send(LIGHT_PURPLE + "[OpenServer] " + GRAY + "Reloaded OpenServer config.");
            openServer.reload();
        })) return;
        if (context.subCommandAt(0, "status", true, (ctx) -> {
            context.send(LIGHT_PURPLE + "[OpenServer] " + GRAY + "Enabled: " + openServer.isEnabled());
        })) return;
        if (context.hasArgs()) context.tooManyArgs();
        else {
            broadcast(LIGHT_PURPLE + "[OpenServer] " + GRAY + "OpenServer is now " + (openServer.isEnabled() ? "disabled." : "enabled."), "greenfieldcore.openserver.message");
            openServer.setEnabled(!openServer.isEnabled());
        }
    }

    private void lock(CommandContext context) throws BCIException {
        if (context.hasArgs()) context.tooManyArgs();
        if (openServer.isEnabled()) throw new BCIException(RED + "OpenServer already enabled, do /unlock to unlock the server.");

        openServer.setEnabled(true);
        broadcast(LIGHT_PURPLE + "[OpenServer] " + GRAY + "OpenServer is now enabled.", "greenfieldcore.openserver.message");
    }

    private void unlock(CommandContext context) throws BCIException {
        if (context.hasArgs()) context.tooManyArgs();
        if (!openServer.isEnabled()) throw new BCIException(RED + "OpenServer already disabled, do /lock to lock the server.");

        openServer.setEnabled(false);
        broadcast(LIGHT_PURPLE + "[OpenServer] " + GRAY + "OpenServer is now disabled.", "greenfieldcore.openserver.message");

    }

    private void completion(TabContext context) {
        context.completionIf((ctx) -> ctx.isLength(1) && !ctx.getAlias().equalsIgnoreCase("lock") && !ctx.getAlias().equalsIgnoreCase("unlock"), "reload", "status");
    }


}
