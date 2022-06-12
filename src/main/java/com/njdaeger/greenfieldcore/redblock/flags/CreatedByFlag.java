package com.njdaeger.greenfieldcore.redblock.flags;

public class CreatedByFlag extends AbstractUUIDFlag {

    public CreatedByFlag() {
        super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Filter who the redblock was created by", "-createdBy <playerName>", "createdBy");
    }

}
