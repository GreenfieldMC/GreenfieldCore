package net.greenfieldmc.core.templates.models;

public enum PasteOption {

    PASTE_ENTITIES("E"),
    PASTE_BIOMES("B"),
    SKIP_AIR("A");

    private final String chatName;

    PasteOption(String chatName) {
        this.chatName = chatName;
    }

    public String getChatName() {
        return chatName;
    }

}
