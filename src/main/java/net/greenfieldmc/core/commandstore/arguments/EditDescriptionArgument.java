package net.greenfieldmc.core.commandstore.arguments;

import net.greenfieldmc.core.commandstore.services.ICommandStoreService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.defaults.QuotedStringArgument;

import java.util.List;

public class EditDescriptionArgument extends QuotedStringArgument {

    private final ICommandStoreService commandStoreService;

    public EditDescriptionArgument(ICommandStoreService commandStoreService) {
        super(false, () -> "The description of the command");
        this.commandStoreService = commandStoreService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext ctx) {
        var server = ctx.argAtOrDefault(0, "").equalsIgnoreCase("server");
        if (server) {
            var id = Integer.parseInt(ctx.argAt(1));
            var command = commandStoreService.getServerStorage().getCommand(id);
            if (command != null) return List.of(command.getDescription());
        } else if (ctx.isPlayer()){
            var id = Integer.parseInt(ctx.argAt(0));
            var command = commandStoreService.getUserStorage(ctx.asPlayerOrNull().getUniqueId()).getCommand(id);
            if (command != null) return List.of(command.getDescription());
        }
        return List.of();
    }
}
