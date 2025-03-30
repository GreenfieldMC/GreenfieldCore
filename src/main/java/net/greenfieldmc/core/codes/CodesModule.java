package net.greenfieldmc.core.codes;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.codes.services.CodesCommandService;
import net.greenfieldmc.core.codes.services.CodesServiceImpl;
import net.greenfieldmc.core.codes.services.ICodesService;

import java.util.function.Predicate;

public class CodesModule extends Module {

    private ICodesService codesService;

    public CodesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        codesService = enableIntegration(new CodesServiceImpl(plugin, this), true);
        enableIntegration(new CodesCommandService(plugin, this, codesService), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(codesService);
    }

}
