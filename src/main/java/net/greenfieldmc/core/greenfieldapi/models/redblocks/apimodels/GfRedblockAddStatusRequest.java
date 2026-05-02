package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

public class GfRedblockAddStatusRequest {

    // String status, long createdBy
    private String status;
    private long createdBy;

    public GfRedblockAddStatusRequest(String status, long createdBy) {
        this.status = status;
        this.createdBy = createdBy;
    }

}
