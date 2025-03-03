package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;

import java.text.DateFormat;
import java.time.Instant;
import java.util.*;

import static com.njdaeger.greenfieldcore.Util.resolvePlayerName;

public class Redblock implements PageItem<ICommandContext> {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);

    private boolean hasChanged = false;

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
    private List<UUID> displayEntityIds;
    private final Location boxUpperBound;
    private final Location boxLowerBound;

    public Redblock(int id, String content, Status status, Location location, UUID createdBy, long createdOn, UUID approvedBy, long approvedOn, UUID completedBy, long completedOn, UUID assignedTo, long assignedOn, String minRank, List<UUID> displayEntityIds) {
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
        this.displayEntityIds = displayEntityIds;
        this.boxUpperBound = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 1, location.getBlockZ() + 1);
        this.boxLowerBound = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 3, location.getBlockZ() - 1);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.hasChanged = true;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.hasChanged = true;
    }

    public UUID getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(UUID completedBy) {
        this.completedBy = completedBy;
        this.hasChanged = true;
    }

    public long getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(long completedOn) {
        this.completedOn = completedOn;
        this.hasChanged = true;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
        this.hasChanged = true;
    }

    public long getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(long approvedOn) {
        this.approvedOn = approvedOn;
        this.hasChanged = true;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        this.hasChanged = true;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
        this.hasChanged = true;
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
        this.hasChanged = true;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
        this.hasChanged = true;
    }

    public long getAssignedOn() {
        return assignedOn;
    }

    public void setAssignedOn(long assignedOn) {
        this.assignedOn = assignedOn;
        this.hasChanged = true;
    }

    public List<UUID> getDisplayEntityIds() {
        return displayEntityIds;
    }

    public void setDisplayEntityIds(List<UUID> displayEntityIds) {
        this.displayEntityIds = displayEntityIds;
        this.hasChanged = true;
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

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
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
                ", armorstands={" + displayEntityIds.stream().map(UUID::toString).reduce("", (a, b) -> a + ", " + b) +
                "}}";
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return content;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var hover = Component.text();
        getRedblockInfo().forEach(infoLine -> {
            hover.append(infoLine.getItemText(paginator, generatorInfo)).appendNewline();
        });
        if (generatorInfo.isLocatable()) {
            Location location = null;
            try {
                location = generatorInfo.getLocation();
            } catch (PDKCommandException ignored) {
            }
            hover.append(Component.text("Distance: ", NamedTextColor.BLUE))
                    .append(Component.text(String.format("%.2f", location.distance(getLocation())), paginator.getGrayColor()))
                    .appendNewline();
        }

        hover.append(Component.text("ID: ", NamedTextColor.BLUE))
                .append(Component.text(getId(), paginator.getGrayColor()));

        var line = Component.text("?", switch (status) {
            case DELETED -> NamedTextColor.DARK_RED;
            case INCOMPLETE -> NamedTextColor.RED;
            case PENDING -> NamedTextColor.GOLD;
            case APPROVED -> NamedTextColor.GREEN;
        }, TextDecoration.BOLD).hoverEvent(HoverEvent.showText(hover)).toBuilder();

        line.appendSpace();
        line.append(Component.text("[T]", NamedTextColor.BLUE, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/rbtp -id " + getId()))
                .hoverEvent(HoverEvent.showText(Component.text("Teleport to this RedBlock", NamedTextColor.GRAY))));

        line.append(Component.text(" - ", paginator.getGrayColor()));
        line.append(Component.text(getContent(), paginator.getGrayColor()));
        return line.build();
    }

    public List<RedblockInfo> getRedblockInfo() {
        var infoLines = new ArrayList<RedblockInfo>();
        infoLines.add(new RedblockInfo("Status", status.name()));
        infoLines.add(new RedblockInfo("Location", location.getWorld().getName() + ", x:" + location.getBlockX() + " y:" + location.getBlockY() + " z:" + location.getBlockZ()));
        if (minRank != null) {
            infoLines.add(new RedblockInfo("MinimumRank", minRank));
        }
        infoLines.add(new RedblockInfo(new RedblockInfo("CreatedBy", resolvePlayerName(createdBy)), new RedblockInfo("CreatedOn", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(createdOn))))));

        if (assignedTo != null)
            infoLines.add(new RedblockInfo(new RedblockInfo("AssignedTo", resolvePlayerName(assignedTo)), new RedblockInfo("AssignedOn", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(assignedOn))))));

        if (approvedBy != null)
            infoLines.add(new RedblockInfo(new RedblockInfo("ApprovedBy", resolvePlayerName(approvedBy)), new RedblockInfo("ApprovedOn", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(approvedOn))))));

        if (completedBy != null)
            infoLines.add(new RedblockInfo(new RedblockInfo("CompletedBy", resolvePlayerName(completedBy)), new RedblockInfo("CompletedOn", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(completedOn))))));

        return infoLines;
    }

    public enum Status {
        INCOMPLETE,
        PENDING,
        APPROVED,
        DELETED
    }

    public class RedblockInfo implements PageItem<ICommandContext> {

        private final String infoKey;
        private final String infoValue;

        private final RedblockInfo[] multiInfoLine;

        public RedblockInfo(String infoKey, String infoValue) {
            this.infoKey = infoKey;
            this.infoValue = infoValue;
            this.multiInfoLine = null;
        }

        public RedblockInfo(RedblockInfo... multiInfoLine) {
            this.infoKey = null;
            this.infoValue = null;
            this.multiInfoLine = multiInfoLine;
        }

        @Override
        public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            if (multiInfoLine != null) {
                return String.join(", ", Arrays.stream(multiInfoLine).map(i -> i.infoKey + ": " + i.infoValue).toList());
            }
            return infoKey + ": " + infoValue;
        }

        @Override
        public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            if (multiInfoLine != null) {
                var builder = Component.text();
                for (var i : multiInfoLine) {
                    if (i.infoKey == null || i.infoValue == null) continue;
                    builder.append(Component.text(i.infoKey + ": ", NamedTextColor.BLUE).append(Component.text(i.infoValue, paginator.getGrayColor())));
                }
                return builder.build();
            }
            if (infoKey == null || infoValue == null) return Component.empty();
            return Component.text(infoKey + ": ", NamedTextColor.BLUE).append(Component.text(infoValue, paginator.getGrayColor()));
        }
    }
}
