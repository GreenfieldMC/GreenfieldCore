package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class Redblock implements PageItem<CommandContext> {

    private String content;
    private final int id;
    private Status status;
    private UUID completedBy;
    private long completedOn;
    private UUID approvedBy;
    private long approvedOn;
    private UUID createdBy;
    private long createdOn;
    private final Location location;
    private String minRank;
    private UUID assignedTo;
    private long assignedOn;
    private List<UUID> armorstands;
    private final Location boxUpperBound;
    private final Location boxLowerBound;

    public Redblock(int id, String content, Status status, Location location, UUID createdBy, long createdOn, UUID approvedBy, long approvedOn, UUID completedBy, long completedOn, UUID assignedTo, long assignedOn, String minRank, List<UUID> armorstands) {
        this.id = id;
        this.content = content;
        this.status = status;
        this.completedBy = completedBy;
        this.completedOn = completedOn;
        this.approvedBy = approvedBy;
        this.approvedOn = approvedOn;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.location = location;
        this.minRank = minRank;
        this.assignedTo = assignedTo;
        this.assignedOn = assignedOn;
        this.armorstands = armorstands;
        this.boxUpperBound = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 1, location.getBlockZ() + 1);
        this.boxLowerBound = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 3, location.getBlockZ() - 1);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UUID getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(UUID completedBy) {
        this.completedBy = completedBy;
    }

    public long getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(long completedOn) {
        this.completedOn = completedOn;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public long getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(long approvedOn) {
        this.approvedOn = approvedOn;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isPartOfRedblock(Location location) {
        if (location.getWorld() != this.location.getWorld()) return false;
        if (this.location.equals(location)) return true;
        //check if the location is within the box created by the upper and lower bound
        if (location.getBlockX() >= boxLowerBound.getBlockX() && location.getBlockX() <= boxUpperBound.getBlockX()) {
            if (location.getBlockY() >= boxLowerBound.getBlockY() && location.getBlockY() <= boxUpperBound.getBlockY()) {
                return location.getBlockZ() >= boxLowerBound.getBlockZ() && location.getBlockZ() <= boxUpperBound.getBlockZ();
            }
        }
        return false;
    }

    public String getMinRank() {
        return minRank;
    }

    public void setMinRank(String minRank) {
        this.minRank = minRank;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public long getAssignedOn() {
        return assignedOn;
    }

    public void setAssignedOn(long assignedOn) {
        this.assignedOn = assignedOn;
    }

    public List<UUID> getArmorstands() {
        return armorstands;
    }

    public void setArmorstands(List<UUID> armorstands) {
        this.armorstands = armorstands;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public boolean isIncomplete() {
        return status == Status.INCOMPLETE;
    }

    @Override
    public String toString() {
        return "Redblock{" +
                "content='" + content + '\'' +
                ", id=" + id +
                ", status=" + status.name() +
                ", completedBy=" + completedBy +
                ", completedOn=" + completedOn +
                ", approvedBy=" + approvedBy +
                ", approvedOn=" + approvedOn +
                ", createdBy=" + createdBy +
                ", createdOn=" + createdOn +
                ", location=" + location.toString() +
                ", minRank=" + minRank +
                ", assignedTo=" + assignedTo +
                ", assignedOn=" + assignedOn +
                ", armorstands={" + armorstands.stream().map(UUID::toString).reduce("", (a, b) -> a + ", " + b) +
                "}}";
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, CommandContext> paginator, CommandContext generatorInfo) {
        return content;
    }

    public enum Status {
        INCOMPLETE,
        PENDING,
        APPROVED,
        DELETED
    }
}
