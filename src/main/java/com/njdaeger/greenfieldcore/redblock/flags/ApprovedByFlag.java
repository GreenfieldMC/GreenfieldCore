package com.njdaeger.greenfieldcore.redblock.flags;

public class ApprovedByFlag extends AbstractUUIDFlag{

    public ApprovedByFlag() {
        super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Filter who the redblock was approved by", "-approvedBy <playerName>", "approvedBy");
    }
}
