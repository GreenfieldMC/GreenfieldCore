package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import java.util.Date;

public class GfRedblockRoleAssignment {

    private String roleName;

    private long createdBy;

    private Date createdOn;

     public GfRedblockRoleAssignment() {
    }

    public GfRedblockRoleAssignment(String roleName, long createdBy, Date createdOn) {
        this.roleName = roleName;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public String getRoleName() {
        return roleName;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

}
