package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.advancedbuild.services.AdvBuildCommandService;
import com.njdaeger.greenfieldcore.advancedbuild.services.AdvBuildServiceImpl;
import com.njdaeger.greenfieldcore.advancedbuild.services.IAdvBuildService;
import com.njdaeger.greenfieldcore.services.CoreProtectServiceImpl;
import com.njdaeger.greenfieldcore.services.WorldEditServiceImpl;

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
