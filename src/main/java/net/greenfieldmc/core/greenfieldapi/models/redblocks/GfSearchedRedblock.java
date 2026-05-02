package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class GfSearchedRedblock {

    private String key;

    private String message;

    private String status;

    private int x;

    private int y;

    private int z;

    private long createdBy;

    @Nullable
    private Date createdOn;

    @Nullable
    private Long updatedBy;

    @Nullable
    private Date updatedOn;

    @Nullable
    private Long deletedBy;

    @Nullable
    private Date deletedOn;

    @Nullable
    private Double distance;

    public GfSearchedRedblock() {
    }

    public GfSearchedRedblock(String key, String message, String status, int x, int y, int z, long createdBy, Date createdOn, Long updatedBy, Date updatedOn, Long deletedBy, Date deletedOn, Double distance) {
        this.key = key;
        this.message = message;
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.deletedBy = deletedBy;
        this.deletedOn = deletedOn;
        this.distance = distance;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    @Nullable
    public Date getCreatedOn() {
        return createdOn;
    }

    @Nullable
    public Long getUpdatedBy() {
        return updatedBy;
    }

    @Nullable
    public Date getUpdatedOn() {
        return updatedOn;
    }

    @Nullable
    public Long getDeletedBy() {
        return deletedBy;
    }

    @Nullable
    public Date getDeletedOn() {
        return deletedOn;
    }

    @Nullable
    public Double getDistance() {
        return distance;
    }




}
