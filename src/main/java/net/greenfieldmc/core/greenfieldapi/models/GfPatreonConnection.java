package net.greenfieldmc.core.greenfieldapi.models;

import java.util.Date;

public class GfPatreonConnection {

    private long userPatreonConnectionId;
    private GfUser user;
    private Date connectedOn;
    private long patreonConnectionId;
    private String fullName;
    private double pledge;
    private Date updatedOn;
    private Date createdOn;

    public GfPatreonConnection() {
    }

    public GfPatreonConnection(long userPatreonConnectionId, GfUser user, Date connectedOn,
                               long patreonConnectionId, String fullName, double pledge,
                               Date updatedOn, Date createdOn) {
        this.userPatreonConnectionId = userPatreonConnectionId;
        this.user = user;
        this.connectedOn = connectedOn;
        this.patreonConnectionId = patreonConnectionId;
        this.fullName = fullName;
        this.pledge = pledge;
        this.updatedOn = updatedOn;
        this.createdOn = createdOn;
    }

    public long getUserPatreonConnectionId() {
        return userPatreonConnectionId;
    }

    public void setUserPatreonConnectionId(long userPatreonConnectionId) {
        this.userPatreonConnectionId = userPatreonConnectionId;
    }

    public GfUser getUser() {
        return user;
    }

    public void setUser(GfUser user) {
        this.user = user;
    }

    public Date getConnectedOn() {
        return connectedOn;
    }

    public void setConnectedOn(Date connectedOn) {
        this.connectedOn = connectedOn;
    }

    public long getPatreonConnectionId() {
        return patreonConnectionId;
    }

    public void setPatreonConnectionId(long patreonConnectionId) {
        this.patreonConnectionId = patreonConnectionId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getPledge() {
        return pledge;
    }

    public void setPledge(double pledge) {
        this.pledge = pledge;
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
        return "GfPatreonConnection{" +
                "userPatreonConnectionId=" + userPatreonConnectionId +
                ", user=" + user +
                ", connectedOn=" + connectedOn +
                ", patreonConnectionId=" + patreonConnectionId +
                ", fullName='" + fullName + '\'' +
                ", pledge=" + pledge +
                ", updatedOn=" + updatedOn +
                ", createdOn=" + createdOn +
                '}';
    }
}


