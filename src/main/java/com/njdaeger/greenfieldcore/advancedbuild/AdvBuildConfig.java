package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdvBuildConfig extends Configuration {

    private List<UUID> enabled;

    public AdvBuildConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "advancedbuildmode");

        addEntry("enabled", new ArrayList<>());
        enabled = getStringList("enabled").stream().map(UUID::fromString).collect(Collectors.toList());

    }

    @Override
    public void save() {
        setEntry("enabled", enabled.stream().map(UUID::toString).collect(Collectors.toList()));
        super.save();
    }

    public List<UUID> getEnabled() {
        return enabled;
    }

    public boolean isEnabledFor(Player player) {
        return enabled.contains(player.getUniqueId());
    }

    public void setEnabled(Player player, boolean enabled) {
        if (enabled) this.enabled.add(player.getUniqueId());
        else this.enabled.remove(player.getUniqueId());
    }

}
