package net.greenfieldmc.core.templates.models;

import org.bukkit.block.BlockFace;

public enum FlipOption implements AdjustableOption<BlockFace> {

    NEG_X("-x", "Randomly flip this template on the negative X axis.", BlockFace.WEST),
    POS_X("x", "Randomly flip this template on the positive X axis.", BlockFace.EAST),
    NEG_Z("-z", "Randomly flip this template on the negative Z axis.", BlockFace.NORTH),
    POS_Z("z", "Randomly flip this template on the positive Z axis.", BlockFace.SOUTH);

    private final String chatName;
    private final String description;
    private final BlockFace flipDirection;

    FlipOption(String chatName, String description, BlockFace flipDirection) {
        this.chatName = chatName;
        this.description = description;
        this.flipDirection = flipDirection;
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
    public BlockFace getAdjustmentValue() {
        return flipDirection;
    }

}
