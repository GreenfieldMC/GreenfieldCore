package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

public class UtilitiesModule extends Module implements Listener {

    private boolean badBlue = false;

    public UtilitiesModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        new UtilityCommands(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {

    }


    public boolean isBadBlue() {
        return badBlue;
    }

    public void setBadBlue(boolean badBlue) {
        this.badBlue = badBlue;
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        if (isBadBlue() && e.getPlayer().getName().equalsIgnoreCase("Bluecolty")) {
            String message = e.getMessage();
            message = message.replaceAll(Pattern.compile("(frick|fric)\\W", Pattern.CASE_INSENSITIVE).pattern(), "fuck");
            message = message.replaceAll(Pattern.compile("(silly sausage)\\W", Pattern.CASE_INSENSITIVE).pattern(), "cunthead");
            message = message.replaceAll(Pattern.compile("(dang|dam)\\W", Pattern.CASE_INSENSITIVE).pattern(), "damn");
            message = message.replaceAll(Pattern.compile("(bri)\\W", Pattern.CASE_INSENSITIVE).pattern(), "bri you slut");
            message = message.replaceAll(Pattern.compile("(heck)\\W", Pattern.CASE_INSENSITIVE).pattern(), "hell");
            message = message.replaceAll(Pattern.compile("(dang|dam)\\W", Pattern.CASE_INSENSITIVE).pattern(), "damn");
            message = message.replaceAll(Pattern.compile("(butt)\\W", Pattern.CASE_INSENSITIVE).pattern(), "ass");
            message = message.replaceAll(Pattern.compile("(crap|poop|shoot)\\W", Pattern.CASE_INSENSITIVE).pattern(), "shit");
            e.setMessage(message);
        }
    }

}
