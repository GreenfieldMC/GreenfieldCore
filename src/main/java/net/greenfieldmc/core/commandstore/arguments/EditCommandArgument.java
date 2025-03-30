package net.greenfieldmc.core.commandstore.arguments;

import net.greenfieldmc.core.commandstore.services.ICommandStoreService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;

import java.util.List;

public class EditCommandArgument extends CommandArgument {

    private final ICommandStoreService commandStoreService;

    public EditCommandArgument(ICommandStoreService commandStoreService) {
        this.commandStoreService = commandStoreService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext ctx) {
        var server = ctx.argAtOrDefault(0, "").equalsIgnoreCase("server");
        if (server) {
            var id = Integer.parseInt(ctx.argAt(1));
            var command = commandStoreService.getServerStorage().getCommand(id);
            if (command != null) return List.of(command.getCommand());
        } else if (ctx.isPlayer()){
            var id = Integer.parseInt(ctx.argAt(0));
            var command = commandStoreService.getUserStorage(ctx.asPlayerOrNull().getUniqueId()).getCommand(id);
            if (command != null) return List.of(command.getCommand());
        }
        return List.of();
    }

}
