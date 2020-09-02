package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.types.ParsedType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerParser extends ParsedType<Player> {

    @Override
    public Player parse(String input) throws PDKCommandException {
        Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (input == null || player == null) throw new PDKCommandException(ChatColor.RED + "Unable to find player " + input);
        else return player;
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }
}
