package net.greenfieldmc.core.templates.models;

public enum RotationOption implements AdjustableOption<Integer> {

    SELF("S", "Rotate according to your player's facing direction.", -1),
    DEG0("0", "Randomly rotate the template 0 degrees.", 0),
    DEG90("90", "Randomly rotate the template 90 degrees.", 90),
    DEG180("180", "Randomly rotate the template 180 degrees.", 180),
    DEG270("270", "Randomly rotate the template 270 degrees.", 270);

    private final String chatName;
    private final int rotation;
    private final String description;

    RotationOption(String chatName, String description, int rotation) {
        this.chatName = chatName;
        this.rotation = rotation;
        this.description = description;
    }

    @Override
    public String getChatName() {
        return chatName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Integer getAdjustmentValue() {
        return rotation;
    }

}
