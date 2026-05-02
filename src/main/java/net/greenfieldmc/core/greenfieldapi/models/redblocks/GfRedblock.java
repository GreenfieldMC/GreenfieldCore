package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GfRedblock {

    private String key;

    private String message;

    private int x;

    private int y;

    private int z;

    private long createdBy;

    private Date createdOn;

    @Nullable
    private Long updatedBy;

    @Nullable
    private Date updatedOn;

    @Nullable
    private Long deletedBy;

    @Nullable
    private Date deletedOn;

    private GfRedblockProject project;

    private List<GfRedblockStatus> statuses;

    private List<GfRedblockUserAssignment> userAssignments;

    private List<GfRedblockRoleAssignment> roleAssignments;

    private List<UUID> entities;

    public GfRedblock() {
    }

    public GfRedblock(String key, String message, int x, int y, int z, long createdBy, Date createdOn, @Nullable Long updatedBy, @Nullable Date updatedOn, @Nullable Long deletedBy, @Nullable Date deletedOn, GfRedblockProject project, List<GfRedblockStatus> statuses, List<GfRedblockUserAssignment> userAssignments, List<GfRedblockRoleAssignment> roleAssignments, List<UUID> entities) {
        this.key = key;
        this.message = message;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.deletedBy = deletedBy;
        this.deletedOn = deletedOn;
        this.project = project;
        this.statuses = statuses;
        this.userAssignments = userAssignments;
        this.roleAssignments = roleAssignments;
        this.entities = entities;
    }

    /**
     * Gets the unique key of this Redblock. PROJECT-ID
     * @return the unique key of this Redblock
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the message associated with this Redblock.
     * @return the message of this Redblock
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the X coordinate of this Redblock.
     * @return the X coordinate of this Redblock
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of this Redblock.
     * @return the Y coordinate of this Redblock
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the Z coordinate of this Redblock.
     * @return the Z coordinate of this Redblock
     */
    public int getZ() {
        return z;
    }

    /**
     * Gets the ID of the user who created this Redblock.
     * @return the ID of the user who created this Redblock
     */
    public long getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the date and time when this Redblock was created.
     * @return the date and time when this Redblock was created
     */
    public Date getCreatedOn() {
        return createdOn;
    }


    /**
     * Gets the ID of the user who last updated this Redblock, or null if it has never been updated.
     * @return the ID of the user who last updated this Redblock, or null if it has never been updated
     */
    @Nullable
    public Long getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Gets the date and time when this Redblock was last updated, or null if it
     * @return the date and time when this Redblock was last updated, or null if it has never been updated
     */
    @Nullable
    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Gets the ID of the user who deleted this Redblock, or null if it has never been deleted.
     * @return the ID of the user who deleted this Redblock, or null if it has never been deleted
     */
    @Nullable
    public Long getDeletedBy() {
        return deletedBy;
    }

    /**
     * Gets the date and time when this Redblock was deleted, or null if it has never been deleted.
     * @return the date and time when this Redblock was deleted, or null if it has never been deleted
     */
    @Nullable
    public Date getDeletedOn() {
        return deletedOn;
    }

    /**
     * Gets the project associated with this Redblock.
     * @return the project associated with this Redblock
     */
    public GfRedblockProject getProject() {
        return project;
    }

    /**
     * Gets the list of statuses associated with this Redblock.
     * @return the list of statuses associated with this Redblock
     */
    public List<GfRedblockStatus> getStatuses() {
        return statuses;
    }

    /**
     * Gets the list of user assignments associated with this Redblock.
     * @return the list of user assignments associated with this Redblock
     */
    public List<GfRedblockUserAssignment> getUserAssignments() {
        return userAssignments;
    }

    /**
     * Gets the list of role assignments associated with this Redblock.
     * @return the list of role assignments associated with this Redblock
     */
    public List<GfRedblockRoleAssignment> getRoleAssignments() {
        return roleAssignments;
    }

    /**
     * Gets the list of entities associated with this Redblock.
     * @return the list of entities associated with this Redblock
     */
    public List<UUID> getEntities() {
        return entities;
    }

}
