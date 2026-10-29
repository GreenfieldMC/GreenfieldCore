package net.greenfieldmc.core.shared.services;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IEssentialsService {

    void setUserLastLocation(Player player, Location location);

}
