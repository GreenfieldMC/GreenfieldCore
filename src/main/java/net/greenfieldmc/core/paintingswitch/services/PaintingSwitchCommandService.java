package net.greenfieldmc.core.paintingswitch.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.paintingswitch.PaintingSwitchMessages;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import org.bukkit.plugin.Plugin;

public class PaintingSwitchCommandService extends ModuleService<PaintingSwitchCommandService> implements IModuleService<PaintingSwitchCommandService> {

    private final IPaintingSwitchService paintingSwitchService;

    public PaintingSwitchCommandService(Plugin plugin, Module module, IPaintingSwitchService paintingSwitchService) {
        super(plugin, module);
        this.paintingSwitchService = paintingSwitchService;
    }

    private void toggle(ICommandContext ctx) throws CommandSenderTypeException {
        var player = ctx.asPlayer();
        var enabled = paintingSwitchService.isEnabledFor(player.getUniqueId());
        paintingSwitchService.setEnabledFor(player.getUniqueId(), !enabled);
        ctx.send(!enabled ? PaintingSwitchMessages.ENABLED : PaintingSwitchMessages.DISABLED);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("pstoggle", "paintingswitch")
                .permission("greenfieldcore.paintingswitch.use")
                .description("Toggle the painting switch feature.")
                .canExecute(this::toggle)
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
