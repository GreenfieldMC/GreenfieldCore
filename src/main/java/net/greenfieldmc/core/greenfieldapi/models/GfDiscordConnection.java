package net.greenfieldmc.core.greenfieldapi.models;

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

    public void setUserDiscordConnectionId(long userDiscordConnectionId) {
        this.userDiscordConnectionId = userDiscordConnectionId;
    }

    public GfUser getUser() {
        return user;
    }

    public void setUser(GfUser user) {
        this.user = user;
    }

    public long getDiscordConnectionId() {
        return discordConnectionId;
    }

    public void setDiscordConnectionId(long discordConnectionId) {
        this.discordConnectionId = discordConnectionId;
    }

    public BigInteger getDiscordSnowflake() {
        return discordSnowflake;
    }

    public void setDiscordSnowflake(BigInteger discordSnowflake) {
        this.discordSnowflake = discordSnowflake;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public void setDiscordUsername(String discordUsername) {
        this.discordUsername = discordUsername;
    }

    public Date getConnectedOn() {
        return connectedOn;
    }

    public void setConnectedOn(Date connectedOn) {
        this.connectedOn = connectedOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
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


