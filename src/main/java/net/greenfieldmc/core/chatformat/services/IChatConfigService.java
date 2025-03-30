package net.greenfieldmc.core.chatformat.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.chatformat.ChatUser;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public interface IChatConfigService extends IModuleService<IChatConfigService> {

    void saveDatabase();

    void save(Player player);

    ChatUser getUser(Player player);

    default void setVolume(Player player, float volume) {
        getUser(player).setVolume(volume);
        save(player);
    }

    default void setSound(Player player, Sound sound) {
        getUser(player).setSound(sound);
        save(player);
    }

    default void setAllowMentions(Player player, boolean allowMentions) {
        getUser(player).setAllowMentions(allowMentions);
        save(player);
    }

    default float getVolume(Player player) {
        return getUser(player).getVolume();
    }

    default Sound getSound(Player player) {
        return getUser(player).getSound();
    }

    default boolean allowsMentions(Player player) {
        return getUser(player).allowsMentions();
    }

}
