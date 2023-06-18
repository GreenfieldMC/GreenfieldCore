package com.njdaeger.greenfieldcore.authhub;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.plugin.Plugin;

public class AuthhubConfig extends Configuration {

    private final int requiredPatreonPledgeCents;

    public AuthhubConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "authhub");

        addEntry("authhub.requiredPatreonPledgeCents", 1000);
        this.requiredPatreonPledgeCents = getInt("authhub.requiredPatreonPledgeCents");
    }

    public int getRequiredPatreonPledge() {
        return requiredPatreonPledgeCents;
    }

}
