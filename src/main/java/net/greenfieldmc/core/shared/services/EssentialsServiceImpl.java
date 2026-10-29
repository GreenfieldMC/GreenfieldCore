package net.greenfieldmc.core.shared.services;

import com.earth2me.essentials.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsServiceImpl implements IEssentialsService {

    private final IEssentials essentials;

    public EssentialsServiceImpl() throws Exception {
        var essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsPlugin == null) throw new Exception("Essentials not found");
        this.essentials = (IEssentials) essentialsPlugin;
    }

    @Override
    public void setUserLastLocation(Player player, Location location) {
        essentials.getUser(player).setLastLocation(location);
    }

//    @Override
//    public void loadUsernameMap() {
//        var userMap = essentials.getUsers();
//        getModule().getLogger().info("Loading uuid to username map... This could take a bit.");
//        Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).map(userMap::loadUncachedUser).filter(Objects::nonNull).forEach(user -> Util.userNameMap.put(user.getUUID(), user.getLastAccountName()));
//        userMap.getAllUserUUIDs().stream().map(userMap::loadUncachedUser).filter(Objects::nonNull).forEach(user -> Util.userNameMap.put(user.getUUID(), user.getLastAccountName()));
//        getModule().getLogger().info("Loaded " + Util.userNameMap.size() + " usernames.");
//    }
}
