package com.njdaeger.greenfieldcore;

import java.util.logging.Logger;

public class ModuleLogger extends Logger {

    private final String prefix;

    public ModuleLogger(Module module) {
        super(module.plugin.getLogger().getName(), null);
        this.prefix = "[" + module.getClass().getSimpleName() + "] ";
        setParent(module.plugin.getLogger());
    }

    @Override
    public void info(String msg) {
        super.info(prefix + msg);
    }

    @Override
    public void warning(String msg) {
        super.warning(prefix + msg);
    }

    @Override
    public void severe(String msg) {
        super.severe(prefix + msg);
    }

}
