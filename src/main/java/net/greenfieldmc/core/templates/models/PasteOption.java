package net.greenfieldmc.core.templates.models;

public enum PasteOption implements AdjustableOption<Boolean> {

    PASTE_ENTITIES("-e", "Allow this template to paste with entities."),
    PASTE_BIOMES("-b", "Allow this template to paste with biomes."),
    SKIP_AIR("-a", "Skip air blocks when pasting this template."),;

    private final String chatName;
    private final String description;

    PasteOption(String chatName, String description) {
        this.chatName = chatName;
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
    public Boolean getAdjustmentValue() {
        return true;
    }

}
