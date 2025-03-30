package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IModuleService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IEssentialsService extends IModuleService<IEssentialsService> {

    void setUserLastLocation(Player player, Location location);

    void loadUsernameMap();

}
