package com.njdaeger.greenfieldcore.chatformat;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ChatConfig extends Configuration {

    private final Map<UUID, ChatUser> users;

    public ChatConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "chatconfig");
        this.users = new HashMap<>();

        addEntry("users", new ArrayList<>());
        if (hasSection("users")) getSection("users").getKeys(false).stream().map(UUID::fromString).forEach(uuid -> {
            ChatUser user = new ChatUser(uuid);
            user.setAllowMentions(getBoolean("users." + uuid + ".allow-mentions"));
            user.setVolume(getFloat("users." + uuid + ".volume"));
            user.setSound(Sound.valueOf(getString("users." + uuid + ".sound")));
            users.put(uuid, user);
        });

    }

    @Override
    public void save() {
        users.values().forEach(user -> {
            setEntry("users." + user.getUuid() + ".allow-mentions", user.allowsMentions());
            setEntry("users." + user.getUuid() + ".volume", user.getVolume());
            setEntry("users." + user.getUuid() + ".sound", user.getSound().name());
        });
        super.save();
    }

    public void save(Player player) {
        setEntry("users." + player.getUniqueId() + ".allow-mentions", allowsMentions(player));
        setEntry("users." + player.getUniqueId() + ".volume", getVolume(player));
        setEntry("users." + player.getUniqueId() + ".sound", getSound(player).name());
        super.save();
    }

    public ChatUser getUser(Player player) {
        if (!users.containsKey(player.getUniqueId())) {
            ChatUser user = new ChatUser(player.getUniqueId());
            users.put(player.getUniqueId(), user);
            return user;
        } else {
            return users.get(player.getUniqueId());
        }
    }

    public void setVolume(Player player, float volume) {
        getUser(player).setVolume(volume);
        save(player);
    }

    public void setSound(Player player, Sound sound) {
        getUser(player).setSound(sound);
        save(player);
    }

    public void setAllowMentions(Player player, boolean allowMentions) {
        getUser(player).setAllowMentions(allowMentions);
        save(player);
    }

    public float getVolume(Player player) {
        return getUser(player).getVolume();
    }

    public Sound getSound(Player player) {
        return getUser(player).getSound();
    }

    public boolean allowsMentions(Player player) {
        return getUser(player).allowsMentions();
    }

    public static class ChatUser {

        private final UUID uuid;
        private boolean allowMentions;
        private float volume;
        private Sound sound;

        public ChatUser(UUID uuid) {
            this.uuid = uuid;
            this.allowMentions = true;
            this.volume = 0.5f;
            this.sound = Sound.BLOCK_NOTE_BLOCK_CHIME;
        }

        public boolean allowsMentions() {
            return allowMentions;
        }

        public void setAllowMentions(boolean allowMentions) {
            this.allowMentions = allowMentions;
        }

        public UUID getUuid() {
            return uuid;
        }

        public float getVolume() {
            return volume;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

        public Sound getSound() {
            return sound;
        }

        public void setSound(Sound sound) {
            this.sound = sound;
        }
    }

}
