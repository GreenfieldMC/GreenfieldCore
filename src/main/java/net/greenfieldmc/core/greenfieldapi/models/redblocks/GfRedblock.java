package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.serviceprovider.Nullable;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.greenfieldmc.core.redblock.RedblockConstants;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.redblock.services.IRedblockServiceV2;
import net.greenfieldmc.core.shared.services.IUserService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static net.greenfieldmc.core.Util.DATE_FORMAT;

public class GfRedblock implements PageItem<Pair<ICommandContext, IUserService>> {

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
        return RedblockConstants.STATUS_APPROVED.equalsIgnoreCase(status);
    }

    /**
     * Checks whether this Redblock is incomplete
     * @return true if this Redblock is incomplete
     */
    public boolean isIncomplete() {
        var status = getLatestStatus();
        return RedblockConstants.STATUS_INCOMPLETE.equalsIgnoreCase(status);
    }

    /**
     * Checks whether this Redblock is pending
     * @return true if this Redblock is pending
     */
    public boolean isPending() {
        var status = getLatestStatus();
        return RedblockConstants.STATUS_PENDING.equalsIgnoreCase(status);
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
                && (RedblockConstants.STATUS_INCOMPLETE.equalsIgnoreCase(status) || RedblockConstants.STATUS_PENDING.equalsIgnoreCase(status));
    }

    /**
     * Gets the list of user assignments associated with this Redblock.
     * @return the list of user assignments associated with this Redblock
     */
    public List<GfRedblockUserAssignment> getUserAssignments() {
        return userAssignments;
    }

    /**
     * Checks whether a user is assigned to this Redblock.
     * @param user the user to check
     * @return true if the user is assigned to this Redblock
     */
    public boolean isUserAssigned(GfUser user) {
        if (userAssignments == null || user == null) return false;
        return userAssignments.stream().anyMatch(assignment -> assignment.getAssignedTo() == user.getUserId());
    }

    /**
     * Checks whether a user is assigned to this Redblock by their user ID.
     * @param userId the user ID to check
     * @return true if the user is assigned to this Redblock
     */
    public boolean isUserAssigned(long userId) {
        if (userAssignments == null) return false;
        return userAssignments.stream().anyMatch(assignment -> assignment.getAssignedTo() == userId);
    }

    /**
     * Gets the list of role assignments associated with this Redblock.
     * @return the list of role assignments associated with this Redblock
     */
    public List<GfRedblockRoleAssignment> getRoleAssignments() {
        return roleAssignments;
    }

    /**
     * Checks whether a role is assigned to this Redblock.
     * @param role the role to check
     * @return true if the role is assigned to this Redblock
     */
    public boolean isRoleAssigned(String role) {
        if (roleAssignments == null || role == null) return false;
        return roleAssignments.stream().anyMatch(assignment -> assignment.getRoleName().equalsIgnoreCase(role));
    }

    /**
     * Gets the list of entities associated with this Redblock.
     * @return the list of entities associated with this Redblock
     */
    public List<UUID> getEntities() {
        return entities;
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, Pair<ICommandContext, IUserService>> paginator, Pair<ICommandContext, IUserService> generatorInfo) {
        return message;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, Pair<ICommandContext, IUserService>> paginator, Pair<ICommandContext, IUserService> generatorInfo) {
        var hover = Component.text();
        var locationFrom = generatorInfo.getFirst().getLocationOrNull();

        var infoLines = getRedblockInfo(locationFrom, generatorInfo.getSecond());
        for (var i = 0; i < infoLines.size(); i++) {
            if (i > 0) hover.append(Component.newline());
            hover.append(infoLines.get(i).getItemText(paginator, generatorInfo));
        }

        var questionMark = Component.text("?",
                isDeleted() ? NamedTextColor.DARK_RED : (
                isIncomplete() ? NamedTextColor.RED : (
                isPending() ? NamedTextColor.GOLD : (
                isApproved() ? NamedTextColor.GREEN :
                NamedTextColor.DARK_PURPLE ))), TextDecoration.BOLD).hoverEvent(HoverEvent.showText(hover));
        var teleport = Component.text("[T]", NamedTextColor.BLUE, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/rbtp -key " + getKey()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to teleport to this Redblock.", NamedTextColor.GRAY)));

        var line = Component.text();
        line.append(questionMark);
        line.appendSpace().resetStyle();
        line.append(teleport);
        line.appendSpace().resetStyle();
        line.append(Component.text("- ", paginator.getGrayColor()).append(Component.text(getMessage())));

        return line.build();
    }

    /**
     * Builds the info lines describing this redblock.
     * @param distanceFrom If set, a distance line is added and the world name is shown with the location
     * @param userService Used to show usernames in place of user ids. Users that aren't cached yet are shown by id and fetched in the background
     * @return The info lines, in display order
     */
    public List<GfRedblockInfo> getRedblockInfo(@Nullable Location distanceFrom, IUserService userService) {
        var infoLines = new ArrayList<GfRedblockInfo>();
        infoLines.add(new GfRedblockInfo("Key", getKey()));
        infoLines.add(new GfRedblockInfo("Created", DATE_FORMAT.format(getCreatedOn()) + " by " + usernameOf(userService, getCreatedBy())));
        if (getDeletedOn() != null) {
            infoLines.add(new GfRedblockInfo("Deleted", DATE_FORMAT.format(getDeletedOn())
                    + (getDeletedBy() != null ? " by " + usernameOf(userService, getDeletedBy()) : "")));
        }
        infoLines.add(new GfRedblockInfo("Location", (distanceFrom != null ? distanceFrom.getWorld().getName() + ", ": "") +  getVector().getBlockX() + ", " + getVector().getBlockY() + ", " + getVector().getBlockZ()));

        if (distanceFrom != null) {
            var distance = getVector().distance(distanceFrom.toVector());
            infoLines.add(new GfRedblockInfo("Distance", String.format("%.2f blocks away", distance)));
        }

        infoLines.add(new GfRedblockInfo("Statuses", ""));
        // sorted by id first so statuses set in the same minute by the same user read in the order they were set
        var sortedStatuses = getStatuses() == null ? List.<GfRedblockStatus>of()
                : getStatuses().stream().sorted(Comparator.comparingLong(GfRedblockStatus::getStatusId)).toList();
        addActionLines(infoLines, userService, sortedStatuses,
                GfRedblockStatus::getCreatedOn, GfRedblockStatus::getCreatedBy, status -> status.getStatus().toUpperCase());


        if (getUserAssignments() != null && !getUserAssignments().isEmpty()) {
            infoLines.add(new GfRedblockInfo("Assigned Users", ""));
            addActionLines(infoLines, userService, getUserAssignments(),
                    GfRedblockUserAssignment::getCreatedOn, GfRedblockUserAssignment::getCreatedBy, ua -> usernameOf(userService, ua.getAssignedTo()));
        }

        if (getRoleAssignments() != null && !getRoleAssignments().isEmpty()) {
            infoLines.add(new GfRedblockInfo("Assigned Roles", ""));
            addActionLines(infoLines, userService, getRoleAssignments(),
                    GfRedblockRoleAssignment::getCreatedOn, GfRedblockRoleAssignment::getCreatedBy, GfRedblockRoleAssignment::getRoleName);
        }

        return infoLines;
    }

    /**
     * Adds one info line per (minute, user) pair: everything one user did within the same minute is shown together as
     * "time: a, b, c by user". Different users acting in the same minute get separate lines with the same time.
     * Lines are in chronological order; users within a minute are in the order of their first item.
     * @param infoLines The lines to add to
     * @param userService Used to show the acting user's name
     * @param items The items to group, in the order they should be listed
     * @param createdOn Gets when an item was created
     * @param createdBy Gets the user id that created an item
     * @param describe Gets the text shown for an item
     */
    private static <T> void addActionLines(List<GfRedblockInfo> infoLines, IUserService userService, List<T> items,
                                           Function<T, Date> createdOn, ToLongFunction<T> createdBy, Function<T, String> describe) {
        groupByMinute(items, createdOn).forEach((minute, itemsInMinute) -> itemsInMinute.stream()
                .collect(Collectors.groupingBy(createdBy::applyAsLong, LinkedHashMap::new, Collectors.toList()))
                .forEach((userId, itemsByUser) -> {
                    var described = itemsByUser.stream().map(describe).collect(Collectors.joining(", "));
                    infoLines.add(new GfRedblockInfo("  " + DATE_FORMAT.format(minute), described + " by " + usernameOf(userService, userId)));
                }));
    }

    /**
     * Gets the username of a user for display.
     * @param userService The service to look the user up with. If the user isn't cached, this starts a background fetch
     * @param userId The internal GreenfieldCoreApi user id
     * @return The username, or "#userId" if the user isn't known (yet)
     */
    private static String usernameOf(IUserService userService, long userId) {
        var user = userService.getUserById(userId);
        return user != null && user.getUsername() != null ? user.getUsername() : "#" + userId;
    }

    /**
     * Groups items by the minute they were created in, dropping seconds and anything finer.
     * @param items The items to group. Null is treated as an empty list
     * @param createdOn Gets the creation time of an item
     * @return The items keyed by the start of their minute, in chronological order. Items within a minute keep their original order
     */
    private static <T> SortedMap<Date, List<T>> groupByMinute(@Nullable List<T> items, Function<T, Date> createdOn) {
        if (items == null) return new TreeMap<>();
        return items.stream().collect(Collectors.groupingBy(
                item -> Date.from(createdOn.apply(item).toInstant().truncatedTo(ChronoUnit.MINUTES)),
                TreeMap::new,
                Collectors.toList()));
    }

    public static class GfRedblockInfo implements PageItem<Pair<ICommandContext, IUserService>> {

        private final String infoKey;
        private final String infoValue;

        public GfRedblockInfo(String infoKey, String infoValue) {
            this.infoKey = infoKey;
            this.infoValue = infoValue;
        }

        @Override
        public String getPlainItemText(ChatPaginator<?, Pair<ICommandContext, IUserService>> paginator, Pair<ICommandContext, IUserService> generatorInfo) {
            return infoKey + ": " + infoValue;
        }

        @Override
        public TextComponent getItemText(ChatPaginator<?, Pair<ICommandContext, IUserService>> paginator, Pair<ICommandContext, IUserService> generatorInfo) {
            if (infoKey == null || infoValue == null) return Component.empty();
            return Component.text(infoKey + ": ", paginator.getGrayColor())
                    .append(Component.text(infoValue, NamedTextColor.BLUE));
        }
    }
}
