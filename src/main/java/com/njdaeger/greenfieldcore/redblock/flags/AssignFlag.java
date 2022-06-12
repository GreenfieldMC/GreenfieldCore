package com.njdaeger.greenfieldcore.redblock.flags;

public class AssignFlag extends AbstractUUIDFlag {

    public AssignFlag() {
        super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("create"), "The user to assign this redblock to.", "-assign <playerName>", "assign");
    }

}
