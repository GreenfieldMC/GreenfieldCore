package net.greenfieldmc.core.redblock;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.greenfieldmc.core.Util.resolvePlayerName;
import static net.greenfieldmc.core.Util.DATE_FORMAT;

public class Redblock implements PageItem<ICommandContext> {

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
        var locationFrom = generatorInfo.getLocationOrNull();

        var currentLine = new AtomicInteger();
        getRedblockInfo(locationFrom).forEach(infoLine -> {
            hover.append(infoLine.getItemText(paginator, generatorInfo));
            if (currentLine.incrementAndGet() != getRedblockInfo(locationFrom).size()) hover.appendNewline();
        });

        var questionMark = Component.text("?", switch (status) {
            case DELETED -> NamedTextColor.DARK_RED;
            case INCOMPLETE -> NamedTextColor.RED;
            case PENDING -> NamedTextColor.GOLD;
            case APPROVED -> NamedTextColor.GREEN;
        }, TextDecoration.BOLD).hoverEvent(HoverEvent.showText(hover));
        var teleport = Component.text("[T]", NamedTextColor.BLUE, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/rbtp flags: -id " + getId()))
                .hoverEvent(HoverEvent.showText(Component.text("Teleport to this RedBlock", NamedTextColor.GRAY)));

        var line = Component.text();
        line.append(questionMark);
        line.appendSpace().resetStyle();
        line.append(teleport);
        line.appendSpace().resetStyle();
        line.append(Component.text("- ", paginator.getGrayColor()).append(Component.text(getContent())));
        return line.build();
    }

    public List<RedblockInfo> getRedblockInfo(Location distanceFrom) {
        var infoLines = new ArrayList<RedblockInfo>();
        infoLines.add(new RedblockInfo("Status", status.name()));
        infoLines.add(new RedblockInfo("Location", location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
        infoLines.add(new RedblockInfo("Created By", resolvePlayerName(createdBy)));
        infoLines.add(new RedblockInfo("Created On", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(createdOn)))));

        if (assignedTo != null) {
            infoLines.add(new RedblockInfo("Assigned To", resolvePlayerName(assignedTo)));
            infoLines.add(new RedblockInfo("Assigned On", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(assignedOn)))));
        }

        if (approvedBy != null) {
            infoLines.add(new RedblockInfo("Approved By", resolvePlayerName(approvedBy)));
            infoLines.add(new RedblockInfo("Approved On", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(approvedOn)))));
        }
        if (completedBy != null) {
            infoLines.add(new RedblockInfo("Completed By", resolvePlayerName(completedBy)));
            infoLines.add(new RedblockInfo("Completed On", DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(completedOn)))));
        }
        if (minRank != null) infoLines.add(new RedblockInfo("Minimum Rank", minRank));
        if (distanceFrom != null) infoLines.add(new RedblockInfo("Distance", String.format("%.2f", distanceFrom.distance(location))));
        infoLines.add(new RedblockInfo("ID", String.valueOf(id)));

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

        public RedblockInfo(String infoKey, String infoValue) {
            this.infoKey = infoKey;
            this.infoValue = infoValue;
        }

        @Override
        public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            return infoKey + ": " + infoValue;
        }

        @Override
        public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            if (infoKey == null || infoValue == null) return Component.empty();
            return Component.text(infoKey + ": ", paginator.getGrayColor()).append(Component.text(infoValue, NamedTextColor.BLUE));
        }
    }
}
