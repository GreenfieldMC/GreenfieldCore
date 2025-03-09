package com.njdaeger.greenfieldcore.chatformat;

import org.bukkit.Sound;

import java.util.UUID;

public class ChatUser {

    private boolean hasChanged = false;
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

    public boolean hasChanged() {
        return hasChanged;
    }

    public boolean allowsMentions() {
        return allowMentions;
    }

    public void setAllowMentions(boolean allowMentions) {
        this.allowMentions = allowMentions;
        this.hasChanged = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        this.hasChanged = true;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
        this.hasChanged = true;
    }

}
