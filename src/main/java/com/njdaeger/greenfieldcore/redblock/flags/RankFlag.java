package com.njdaeger.greenfieldcore.redblock.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.flag.Flag;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

import java.util.stream.Stream;

public class RankFlag extends Flag<String> {

    private final Permission permission;

    public RankFlag() {
        super(String.class, "The minimum rank this redblock is to be completed by", "-rank <rank>", "rank");

        var rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            throw new IllegalStateException("Vault is not installed!");
        } else this.permission = rsp.getProvider();
    }

    @Override
    public String parse(CommandContext context, String argument) {
        //if permission groups array does not contain the argument, throw an exception
        if (Stream.of(permission.getGroups()).noneMatch(arg -> arg.equalsIgnoreCase(argument))) {
            return null;
        }
        return argument;
    }

    @Override
    public void complete(TabContext context) {
        context.completion(permission.getGroups());
    }
}
