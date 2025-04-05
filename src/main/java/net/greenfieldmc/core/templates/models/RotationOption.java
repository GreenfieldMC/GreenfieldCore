package net.greenfieldmc.core.templates.models;

public enum RotationOption {

    SELF("SELF", -1),
    DEG0("0", 0),
    DEG90("90", 90),
    DEG180("180", 180),
    DEG270("270", 270);

    private final String chatName;
    private final int rotation;

    RotationOption(String chatName, int rotation) {
        this.chatName = chatName;
        this.rotation = rotation;
    }

    public String getChatName() {
        return chatName;
    }

    public int getRotation() {
        return rotation;
    }

}
