package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import java.util.Date;

public class GfRedblockUserAssignment {

    private long assignedTo;

    private long createdBy;

    private Date createdOn;

    public GfRedblockUserAssignment() {
    }

    public GfRedblockUserAssignment(long assignedTo, long createdBy, Date createdOn) {
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public long getAssignedTo() {
        return assignedTo;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

}
