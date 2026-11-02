package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import com.njdaeger.pdk.serviceprovider.Nullable;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.redblock.services.IRedblockServiceV2;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Comparator;
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

    private final Vector boxUpperBound = new Vector(x + 1, y - 1, z + 1);
    private final Vector boxLowerBound = new Vector(x - 1, y - 3, z - 1);

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
     * Get the world agnostic location of this Redblock.
     * @return the world agnostic location of this Redblock
     */
    public Vector getVector() {
        return new Vector(x, y, z);
    }

    /**
     * Determine if a location is within the bounds of this Redblock. This checks both the sign location and the cube area.
     * @param vector The location to check
     * @return true if the location is within the bounds of this Redblock
     */
    public boolean isPartOfRedblock(Vector vector) {
        if (getVector().equals(vector)) return true; //where the sign is
        return vector.isInAABB(boxLowerBound, boxUpperBound); //where the cube is
    }

    /**
     * Checks whether this Redblock is deleted.
     * @return true if this Redblock is deleted
     */
    public boolean isDeleted() {
        return deletedBy != null || deletedOn != null;
    }

    /**
     * Checks whether this Redblock is approved, meaning its latest status is "approved".
     * @return true if this Redblock is approved
     */
    public boolean isApproved() {
        var status = getLatestStatus();
        return IRedblockServiceV2.STATUS_APPROVED.equalsIgnoreCase(status);
    }

    /**
     * Checks whether this Redblock is incomplete
     * @return true if this Redblock is incomplete
     */
    public boolean isIncomplete() {
        var status = getLatestStatus();
        return IRedblockServiceV2.STATUS_INCOMPLETE.equalsIgnoreCase(status);
    }

    /**
     * Checks whether this Redblock is pending
     * @return true if this Redblock is pending
     */
    public boolean isPending() {
        var status = getLatestStatus();
        return IRedblockServiceV2.STATUS_PENDING.equalsIgnoreCase(status);
    }

    /**
     * Gets the most recent status of this Redblock, or null if it has no statuses.
     * @return the most recent status of this Redblock
     */
    @Nullable
    public String getLatestStatus() {
        if (statuses == null) return null;
        return statuses.stream()
                .max(Comparator.comparingLong(GfRedblockStatus::getStatusId))
                .map(GfRedblockStatus::getStatus)
                .orElse(null);
    }

    /**
     * Checks whether this Redblock is active, meaning it is not deleted and its latest status is incomplete or pending.
     * @return true if this Redblock is active
     */
    public boolean isActive() {
        var status = getLatestStatus();
        return deletedBy == null && deletedOn == null
                && (IRedblockServiceV2.STATUS_INCOMPLETE.equalsIgnoreCase(status) || IRedblockServiceV2.STATUS_PENDING.equalsIgnoreCase(status));
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
