package com.njdaeger.greenfieldcore.shared.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IEssentialsService extends IModuleService<IEssentialsService> {

    void setUserLastLocation(Player player, Location location);

    void loadUsernameMap();

}
