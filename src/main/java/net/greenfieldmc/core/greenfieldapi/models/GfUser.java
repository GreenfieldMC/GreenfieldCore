package net.greenfieldmc.core.greenfieldapi.models;

import java.util.Date;
import java.util.UUID;


/**
 * Represents a user in the Greenfield system.
 */
public class GfUser {

    /**
     * The unique identifier for the user in the Greenfield system.
     */
    private long userId;

    /**
     * The Minecraft UUID of the user
     */
    private UUID minecraftUuid;

    /**
     * The most recently known username of this user.
     */
    private String username;

    /**
     * The date and time when the user was created in the Greenfield system.
     */
    private Date createdOn;

    public GfUser() {
    }

    public GfUser(long userId, UUID minecraftUuid, String username, Date createdOn) {
        this.userId = userId;
        this.minecraftUuid = minecraftUuid;
        this.username = username;
        this.createdOn = createdOn;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }

    public void setMinecraftUuid(UUID minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "GfUser{" +
                "userId=" + userId +
                ", minecraftUuid=" + minecraftUuid +
                ", username='" + username + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }
}


