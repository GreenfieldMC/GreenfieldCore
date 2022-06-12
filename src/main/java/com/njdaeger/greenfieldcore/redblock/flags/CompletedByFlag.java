package com.njdaeger.greenfieldcore.redblock.flags;

public class CompletedByFlag extends AbstractUUIDFlag {

        public CompletedByFlag() {
            super((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Filter who the redblock was completed by", "-completedBy <playerName>", "completedBy");
        }

}
