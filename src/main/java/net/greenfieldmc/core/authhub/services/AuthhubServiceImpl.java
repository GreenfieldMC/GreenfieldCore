package net.greenfieldmc.core.authhub.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;

public class AuthhubServiceImpl implements IAuthhubService {

    private final IConfig config;
    private final int requiredPatreonPledgeCents;

    public AuthhubServiceImpl(IGreenfieldCorePlugin plugin) {
        try {
            this.config = ConfigType.YML.createNew(plugin, "authhub");

            config.addEntry("authhub.requiredPatreonPledgeCents", 1000);
            this.requiredPatreonPledgeCents = config.getInt("authhub.requiredPatreonPledgeCents");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AuthhubService", e);
        }
    }


    @Override
    public int getRequiredPatreonPledge() {
        return requiredPatreonPledgeCents;
    }
}
