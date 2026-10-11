package net.greenfieldmc.core.paintingswitch.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.paintingswitch.PaintingSwitchMessages;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;

public class PaintingSwitchCommandService {

    private final IPaintingSwitchService paintingSwitchService;

    public PaintingSwitchCommandService(IGreenfieldCorePlugin plugin,IPaintingSwitchService paintingSwitchService) {
        this.paintingSwitchService = paintingSwitchService;

        CommandBuilder.of("pstoggle", "paintingswitch")
                .permission("greenfieldcore.paintingswitch.use")
                .description("Toggle the painting switch feature.")
                .canExecute(this::toggle)
                .register(plugin);
    }

    private void toggle(ICommandContext ctx) throws CommandSenderTypeException {
        var player = ctx.asPlayer();
        var enabled = paintingSwitchService.isEnabledFor(player.getUniqueId());
        paintingSwitchService.setEnabledFor(player.getUniqueId(), !enabled);
        ctx.send(!enabled ? PaintingSwitchMessages.ENABLED : PaintingSwitchMessages.DISABLED);
    }
}
