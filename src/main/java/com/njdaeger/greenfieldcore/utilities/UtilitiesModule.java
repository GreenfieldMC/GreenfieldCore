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
            message = message.replaceAll(Pattern.compile("(frick|fric|frik|fricc)", Pattern.CASE_INSENSITIVE).pattern(), "fuck");
            message = message.replaceAll(Pattern.compile("(silly sausage)", Pattern.CASE_INSENSITIVE).pattern(), "cunthead");
            message = message.replaceAll(Pattern.compile("(dang|dam|darn)", Pattern.CASE_INSENSITIVE).pattern(), "damn");
            message = message.replaceAll(Pattern.compile("(bri)\\W", Pattern.CASE_INSENSITIVE).pattern(), "bri you slut");
            message = message.replaceAll(Pattern.compile("(heck|hecc)", Pattern.CASE_INSENSITIVE).pattern(), "hell ");
            message = message.replaceAll(Pattern.compile("(dang|dam)", Pattern.CASE_INSENSITIVE).pattern(), "damn");
            message = message.replaceAll(Pattern.compile("(butt)", Pattern.CASE_INSENSITIVE).pattern(), "ass");
            message = message.replaceAll(Pattern.compile("(crap|poop|shoot)", Pattern.CASE_INSENSITIVE).pattern(), "shit");
            message = message.replaceAll(Pattern.compile("(wack|wac)", Pattern.CASE_INSENSITIVE).pattern(), "damn, thats some wacky bullshit right there");
            e.setMessage(message);
        }
    }

}
