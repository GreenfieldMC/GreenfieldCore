package net.greenfieldmc.core.greenfieldapi.models.users;

import java.math.BigInteger;
import java.util.Date;

public class GfDiscordConnection {

    private long userDiscordConnectionId;
    private GfUser user;
    private long discordConnectionId;
    private BigInteger discordSnowflake;
    private String discordUsername;
    private Date connectedOn;
    private Date updatedOn;
    private Date createdOn;

    public GfDiscordConnection() {
    }

    public GfDiscordConnection(long userDiscordConnectionId, GfUser user, long discordConnectionId,
                               BigInteger discordSnowflake, String discordUsername, Date connectedOn,
                               Date updatedOn, Date createdOn) {
        this.userDiscordConnectionId = userDiscordConnectionId;
        this.user = user;
        this.discordConnectionId = discordConnectionId;
        this.discordSnowflake = discordSnowflake;
        this.discordUsername = discordUsername;
        this.connectedOn = connectedOn;
        this.updatedOn = updatedOn;
        this.createdOn = createdOn;
    }

    public long getUserDiscordConnectionId() {
        return userDiscordConnectionId;
    }

    public GfUser getUser() {
        return user;
    }

    public long getDiscordConnectionId() {
        return discordConnectionId;
    }

    public BigInteger getDiscordSnowflake() {
        return discordSnowflake;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public Date getConnectedOn() {
        return connectedOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    @Override
    public String toString() {
        return "GfDiscordConnection{" +
                "userDiscordConnectionId=" + userDiscordConnectionId +
                ", user=" + user +
                ", discordConnectionId=" + discordConnectionId +
                ", discordSnowflake=" + discordSnowflake +
                ", discordUsername='" + discordUsername + '\'' +
                ", connectedOn=" + connectedOn +
                ", updatedOn=" + updatedOn +
                ", createdOn=" + createdOn +
                '}';
    }
}


