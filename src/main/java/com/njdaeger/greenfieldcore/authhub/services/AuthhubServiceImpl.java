package com.njdaeger.greenfieldcore.authhub.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

public class AuthhubServiceImpl extends ModuleService<IAuthhubService> implements IAuthhubService {

    private IConfig config;
    private int requiredPatreonPledgeCents;

    public AuthhubServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "authhub");

            config.addEntry("authhub.requiredPatreonPledgeCents", 1000);
            this.requiredPatreonPledgeCents = config.getInt("authhub.requiredPatreonPledgeCents");
        } catch (Exception e) {
            throw new Exception("Failed to load AuthhubService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public int getRequiredPatreonPledge() {
        return requiredPatreonPledgeCents;
    }
}
