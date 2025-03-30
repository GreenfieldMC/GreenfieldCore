package net.greenfieldmc.core.advancedbuild;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildCommandService;
import net.greenfieldmc.core.advancedbuild.services.AdvBuildServiceImpl;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;
import net.greenfieldmc.core.shared.services.CoreProtectServiceImpl;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;

import java.util.function.Predicate;

public class AdvancedBuildModule extends Module {

    private IAdvBuildService advBuildService;

    public AdvancedBuildModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() throws Exception {
        try {

            var worldEditService = enableIntegration(new WorldEditServiceImpl(plugin, this), false);
            var coreProtectService = enableIntegration(new CoreProtectServiceImpl(plugin, this), false);
            advBuildService = enableIntegration(new AdvBuildServiceImpl(plugin, this, worldEditService, coreProtectService), true);
            enableIntegration(new AdvBuildCommandService(plugin, this, advBuildService), true);
        } catch (Exception e) {
            throw new Exception("Failed to enable AdvancedBuildModule", e);
        }
    }

    @Override
    public void tryDisable() {
        disableIntegration(advBuildService);
    }

}
