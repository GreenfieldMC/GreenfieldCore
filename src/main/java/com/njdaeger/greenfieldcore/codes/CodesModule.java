package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.codes.services.CodesCommandService;
import com.njdaeger.greenfieldcore.codes.services.CodesServiceImpl;
import com.njdaeger.greenfieldcore.codes.services.ICodesService;

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
