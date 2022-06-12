package com.njdaeger.greenfieldcore.redblock;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class Redblock {

    /*

    Upon creation, the following fields must be set
    - id
    - content
    - status
    - location
    - createdBy
    - createdOn
    - armorstands

    - minrank is optional upon creation
    - assignedTo is optional upon creation
    - assignedOn is optional upon creation

    everything else should be null or 0 upon creation
     */

    private String content;
    private final int id;
    private Status status;
    private UUID completedBy;
    private long completedOn;
    private UUID approvedBy;
    private long approvedOn;
    private UUID createdBy;
    private long createdOn;
    private Location location;
    private String minRank;
    private UUID assignedTo;
    private long assignedOn;
    private List<UUID> armorstands;

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

    public void setLocation(Location location) {
        this.location = location;
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

    public enum Status {
        INCOMPLETE,
        PENDING,
        APPROVED,
        DELETED
    }

}
