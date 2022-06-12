package com.njdaeger.greenfieldcore.redblock.flags;

public class AssignedToFlag extends AbstractUUIDFlag {

    public AssignedToFlag() {
        super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Filter who the redblock was assigned to", "-assignedTo <playerName>", "assignedTo");
    }

}
