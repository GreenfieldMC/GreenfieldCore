package net.greenfieldmc.core.templates.models;

import org.bukkit.block.BlockFace;

public enum FlipOption {

    NEG_X("-x", BlockFace.WEST),
    POS_X("x", BlockFace.EAST),
    NEG_Z("-z", BlockFace.NORTH),
    POS_Z("z", BlockFace.SOUTH);

    private final String chatName;
    private final BlockFace flipDirection;

    FlipOption(String chatName, BlockFace flipDirection) {
        this.chatName = chatName;
        this.flipDirection = flipDirection;
    }

    public String getChatName() {
        return chatName;
    }

    public BlockFace getFlipDirection() {
        return flipDirection;
    }

}
