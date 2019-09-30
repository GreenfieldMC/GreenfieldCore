package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.types.ParsedType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerParser extends ParsedType<Player> {

    @Override
    public Player parse(String input) throws BCIException {
        Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (input == null || player == null) throw new BCIException(ChatColor.RED + "Unable to find player " + input);
        else return player;
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }
}
