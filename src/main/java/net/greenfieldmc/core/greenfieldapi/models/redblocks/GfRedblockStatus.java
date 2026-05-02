package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import java.util.Date;

public class GfRedblockStatus {

    private long statusId;

    private long redblockId;

    private String status;

    private long createdBy;

    private Date createdOn;

    public GfRedblockStatus() {
    }

    public GfRedblockStatus(long statusId, long redblockId, String status, long createdBy, Date createdOn) {
        this.statusId = statusId;
        this.redblockId = redblockId;
        this.status = status;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public long getStatusId() {
        return statusId;
    }

    public long getRedblockId() {
        return redblockId;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

}
