package net.greenfieldmc.core.paintingswitch;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.paintingswitch.services.IPaintingSwitchService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchCommandService;
import net.greenfieldmc.core.paintingswitch.services.PaintingSwitchServiceImpl;
import org.bukkit.event.Listener;

import java.util.function.Predicate;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class PaintingSwitchModule extends Module implements Listener {

    private IPaintingSwitchService paintingSwitchService;

    public PaintingSwitchModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        paintingSwitchService = enableIntegration(new PaintingSwitchServiceImpl(plugin, this), true);
        enableIntegration(new PaintingSwitchCommandService(plugin, this, paintingSwitchService), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(paintingSwitchService);
    }

}
