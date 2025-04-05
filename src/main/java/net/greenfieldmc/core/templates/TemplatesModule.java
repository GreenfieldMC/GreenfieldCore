package net.greenfieldmc.core.templates;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;

import java.util.function.Predicate;

public class TemplatesModule extends Module {

    public TemplatesModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {

    }

    @Override
    protected void tryDisable() throws Exception {

    }
}
