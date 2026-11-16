package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A search for redblocks within a project. Filters are organized into {@link FilterGroup}s: a redblock is returned if
 * it matches at least one group (OR), and within a group every filter that is set must match (AND). With no groups,
 * no filtering is applied.
 */
public class GfRedblockSearchRequest {

    /**
     * The most filter groups the api accepts in one search.
     */
    public static final int MAX_FILTER_GROUPS = 10;

    /**
     * The location to search around. When set, results are ordered by distance from this location, closest first.
     * When null, the search does not specify an order.
     */
    public @Nullable Location location;

    /**
     * Only return redblocks within this many blocks of {@link #location}. Ignored when location is null, and
     * cannot be negative.
     */
    public @Nullable Long radius;

    /**
     * The filter groups of this search. A redblock is returned if it matches at least one group. An empty list applies
     * no filtering. At most {@link #MAX_FILTER_GROUPS} groups are allowed, and the api rejects a group with no filters set.
     */
    public @NotNull List<FilterGroup> filterGroups = new ArrayList<>();

    /**
     * How many redblocks to return per page. Must be at least 1.
     */
    public int resultsPerPage = 50;

    /**
     * The page of results to return, starting at 1.
     */
    public long currentPage = 1;

    /**
     * Adds a filter group to this search. Redblocks matching this group are returned in addition to those matching
     * the other groups.
     * @param group The group to add
     * @return This search request
     */
    public GfRedblockSearchRequest addFilterGroup(@NotNull FilterGroup group) {
        filterGroups.add(group);
        return this;
    }

    /**
     * Applies a change to every filter group currently in this search, for filters that should hold for every
     * alternative (e.g. "not deleted"). Groups added afterwards are not affected, so call this after adding the groups.
     * @param action The change to apply to each group
     * @return This search request
     */
    public GfRedblockSearchRequest forAllGroups(@NotNull Consumer<FilterGroup> action) {
        filterGroups.forEach(action);
        return this;
    }

    /**
     * A set of filters that must all match (AND) for a redblock to match the group. Filters left null are not used.
     */
    public static class FilterGroup {

        /**
         * Filters by the current status of a redblock. See {@link StatusFilter.MatchType}.
         */
        public @Nullable StatusFilter statusFilter;

        /**
         * Filters by whether, and by whom, a redblock was deleted. See {@link DeletionFilter.MatchType}.
         */
        public @Nullable DeletionFilter deletionFilter;

        /**
         * Filters by the users assigned to a redblock. See {@link UserAssignmentFilter.MatchType}.
         */
        public @Nullable UserAssignmentFilter userAssignmentFilter;

        /**
         * Filters by the roles assigned to a redblock. See {@link RoleAssignmentFilter.MatchType}.
         */
        public @Nullable RoleAssignmentFilter roleAssignmentFilter;

        /**
         * Filters by the message of a redblock. See {@link MessageFilter.MatchType}.
         */
        public @Nullable MessageFilter messageFilter;

        public FilterGroup status(@Nullable StatusFilter statusFilter) {
            this.statusFilter = statusFilter;
            return this;
        }

        public FilterGroup deletion(@Nullable DeletionFilter deletionFilter) {
            this.deletionFilter = deletionFilter;
            return this;
        }

        public FilterGroup userAssignment(@Nullable UserAssignmentFilter userAssignmentFilter) {
            this.userAssignmentFilter = userAssignmentFilter;
            return this;
        }

        public FilterGroup roleAssignment(@Nullable RoleAssignmentFilter roleAssignmentFilter) {
            this.roleAssignmentFilter = roleAssignmentFilter;
            return this;
        }

        public FilterGroup message(@Nullable MessageFilter messageFilter) {
            this.messageFilter = messageFilter;
            return this;
        }

        /**
         * @return True if no filter is set in this group. The api rejects empty groups.
         */
        public boolean isEmpty() {
            return statusFilter == null && deletionFilter == null && userAssignmentFilter == null
                    && roleAssignmentFilter == null && messageFilter == null;
        }

    }

    /**
     * The status filter of a redblock search request. Filters by the current (latest) status of a redblock.
     */
    public static class StatusFilter {

        public @NotNull List<String> statuses;
        public @NotNull MatchType matchType;

        public StatusFilter(@NotNull List<String> statuses, @NotNull MatchType matchType) {
            this.statuses = statuses;
            this.matchType = matchType;
        }

        /**
         * @param statuses The statuses to match. No statuses applies no filtering
         * @return A filter that only matches redblocks whose current status is one of the given statuses
         */
        public static StatusFilter anyOf(@NotNull String... statuses) {
            return anyOf(List.of(statuses));
        }

        /**
         * @param statuses The statuses to match. An empty list applies no filtering
         * @return A filter that only matches redblocks whose current status is one of the given statuses
         */
        public static StatusFilter anyOf(@NotNull List<String> statuses) {
            return new StatusFilter(statuses, MatchType.OR);
        }

        /**
         * @param statuses The statuses to exclude. No statuses matches redblocks that have any status
         * @return A filter that only matches redblocks that have a status other than the given statuses
         */
        public static StatusFilter noneOf(@NotNull String... statuses) {
            return noneOf(List.of(statuses));
        }

        /**
         * @param statuses The statuses to exclude. An empty list matches redblocks that have any status
         * @return A filter that only matches redblocks that have a status other than the given statuses
         */
        public static StatusFilter noneOf(@NotNull List<String> statuses) {
            return new StatusFilter(statuses, MatchType.NOT);
        }

        /**
         * @return A filter that applies no filtering by status
         */
        public static StatusFilter noFilter() {
            return new StatusFilter(List.of(), MatchType.OR);
        }

        /**
         * A redblock has at most one current status, so a non-empty status list only ever matches redblocks that have a status.
         * <ul>
         *     <li>OR + [] = no filtering</li>
         *     <li>OR + [...] = the current status is one of the statuses</li>
         *     <li>NOT + [] = the redblock has a status</li>
         *     <li>NOT + [...] = the redblock has a status that is none of the statuses</li>
         * </ul>
         */
        public enum MatchType {

            OR("or"),
            NOT("not");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

    /**
     * The deletion filter of a redblock search request. Filters by whether, and by whom, a redblock was deleted.
     */
    public static class DeletionFilter {

        public @NotNull List<Long> users;
        public @NotNull MatchType matchType;

        public DeletionFilter(@NotNull List<Long> users, @NotNull MatchType matchType) {
            this.users = users;
            this.matchType = matchType;
        }

        /**
         * @return A filter that only matches redblocks that are deleted, regardless of who deleted them
         */
        public static DeletionFilter deletedOnly() {
            return new DeletionFilter(List.of(), MatchType.NOT);
        }

        /**
         * @return A filter that only matches redblocks that are not deleted
         */
        public static DeletionFilter notDeleted() {
            return new DeletionFilter(List.of(), MatchType.AND);
        }

        /**
         * @param userIds The users to match
         * @return A filter that only matches redblocks deleted by any of the given users
         */
        public static DeletionFilter deletedByAny(@NotNull List<Long> userIds) {
            return new DeletionFilter(userIds, MatchType.OR);
        }

        /**
         * @param userIds The users to exclude
         * @return A filter that only matches deleted redblocks that were not deleted by any of the given users
         */
        public static DeletionFilter deletedByNone(@NotNull List<Long> userIds) {
            return new DeletionFilter(userIds, MatchType.NOT);
        }

        /**
         * A redblock has at most one deleter, so a non-empty user list only ever matches deleted redblocks.
         * <ul>
         *     <li>OR + [] = no filtering</li>
         *     <li>OR + [...] = deleted by one of the users</li>
         *     <li>NOT + [] = deleted by anyone</li>
         *     <li>NOT + [...] = deleted by anyone except the users</li>
         *     <li>AND + [] = not deleted</li>
         *     <li>AND + [...] = rejected by the api, a redblock can only be deleted by one user</li>
         * </ul>
         */
        public enum MatchType {
            OR("or"),
            NOT("not"),
            AND("and");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

    /**
     * The user assignment filter of a redblock search request. Filters by who is assigned to a redblock.
     */
    public static class UserAssignmentFilter {

        public @NotNull List<Long> users;
        public @NotNull MatchType matchType;

        public UserAssignmentFilter(@NotNull List<Long> users, @NotNull MatchType matchType) {
            this.users = users;
            this.matchType = matchType;
        }

        /**
         * @param userIds The users to match. An empty list applies no filtering
         * @return A filter that only matches redblocks assigned to at least one of the given users
         */
        public static UserAssignmentFilter assignedToAny(@NotNull List<Long> userIds) {
            return new UserAssignmentFilter(userIds, MatchType.OR);
        }

        /**
         * @return A filter that applies no filtering by user assignment
         */
        public static UserAssignmentFilter noFilter() {
            return new UserAssignmentFilter(List.of(), MatchType.OR);
        }

        /**
         * @param userIds The users to exclude. An empty list matches redblocks that have at least one user assigned
         * @return A filter that only matches redblocks assigned to none of the given users, including unassigned redblocks
         */
        public static UserAssignmentFilter notAssignedTo(@NotNull List<Long> userIds) {
            return new UserAssignmentFilter(userIds, MatchType.NOT);
        }

        /**
         * @return A filter that only matches redblocks that have at least one user assigned
         */
        public static UserAssignmentFilter assigned() {
            return new UserAssignmentFilter(List.of(), MatchType.NOT);
        }

        /**
         * @param userIds The users to match. An empty list matches redblocks that have no users assigned
         * @return A filter that only matches redblocks assigned to every one of the given users
         */
        public static UserAssignmentFilter assignedToAll(@NotNull List<Long> userIds) {
            return new UserAssignmentFilter(userIds, MatchType.AND);
        }

        /**
         * @return A filter that only matches redblocks that have no users assigned
         */
        public static UserAssignmentFilter unassigned() {
            return new UserAssignmentFilter(List.of(), MatchType.AND);
        }

        /**
         * @param userIds The users to match. An empty list matches redblocks that have no users assigned
         * @return A filter that only matches redblocks assigned to every one of the given users and no other users
         */
        public static UserAssignmentFilter assignedToExactly(@NotNull List<Long> userIds) {
            return new UserAssignmentFilter(userIds, MatchType.EXACT);
        }

        /**
         * A redblock can have any number of users assigned, so unlike the status and deletion filters, NOT + [...]
         * also matches redblocks with no users assigned.
         * <ul>
         *     <li>OR + [] = no filtering</li>
         *     <li>OR + [...] = assigned to at least one of the users</li>
         *     <li>NOT + [] = has at least one user assigned</li>
         *     <li>NOT + [...] = assigned to none of the users, including redblocks with no users assigned</li>
         *     <li>AND + [] = has no users assigned</li>
         *     <li>AND + [...] = assigned to every one of the users, other users may also be assigned</li>
         *     <li>EXACT + [] = has no users assigned</li>
         *     <li>EXACT + [...] = assigned to every one of the users and no other users</li>
         * </ul>
         */
        public enum MatchType {
            OR("or"),
            NOT("not"),
            AND("and"),
            EXACT("exact");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

    /**
     * The role assignment filter of a redblock search request. Filters by which roles are assigned to a redblock.
     */
    public static class RoleAssignmentFilter {

        public @NotNull List<String> roles;
        public @NotNull MatchType matchType;

        public RoleAssignmentFilter(@NotNull List<String> roles, @NotNull MatchType matchType) {
            this.roles = roles;
            this.matchType = matchType;
        }

        /**
         * @param roles The roles to match. An empty list applies no filtering
         * @return A filter that only matches redblocks assigned at least one of the given roles
         */
        public static RoleAssignmentFilter assignedToAny(@NotNull List<String> roles) {
            return new RoleAssignmentFilter(roles, MatchType.OR);
        }

        /**
         * @return A filter that applies no filtering by role assignment
         */
        public static RoleAssignmentFilter noFilter() {
            return new RoleAssignmentFilter(List.of(), MatchType.OR);
        }

        /**
         * @param roles The roles to exclude. An empty list matches redblocks that have at least one role assigned
         * @return A filter that only matches redblocks assigned none of the given roles, including redblocks with no roles assigned
         */
        public static RoleAssignmentFilter notAssignedTo(@NotNull List<String> roles) {
            return new RoleAssignmentFilter(roles, MatchType.NOT);
        }

        /**
         * @return A filter that only matches redblocks that have at least one role assigned
         */
        public static RoleAssignmentFilter assigned() {
            return new RoleAssignmentFilter(List.of(), MatchType.NOT);
        }

        /**
         * @param roles The roles to match. An empty list matches redblocks that have no roles assigned
         * @return A filter that only matches redblocks assigned every one of the given roles
         */
        public static RoleAssignmentFilter assignedToAll(@NotNull List<String> roles) {
            return new RoleAssignmentFilter(roles, MatchType.AND);
        }

        /**
         * @return A filter that only matches redblocks that have no roles assigned
         */
        public static RoleAssignmentFilter unassigned() {
            return new RoleAssignmentFilter(List.of(), MatchType.AND);
        }

        /**
         * @param roles The roles to match. An empty list matches redblocks that have no roles assigned
         * @return A filter that only matches redblocks assigned every one of the given roles and no other roles
         */
        public static RoleAssignmentFilter assignedToExactly(@NotNull List<String> roles) {
            return new RoleAssignmentFilter(roles, MatchType.EXACT);
        }

        /**
         * A redblock can have any number of roles assigned, so unlike the status and deletion filters, NOT + [...]
         * also matches redblocks with no roles assigned.
         * <ul>
         *     <li>OR + [] = no filtering</li>
         *     <li>OR + [...] = assigned at least one of the roles</li>
         *     <li>NOT + [] = has at least one role assigned</li>
         *     <li>NOT + [...] = assigned none of the roles, including redblocks with no roles assigned</li>
         *     <li>AND + [] = has no roles assigned</li>
         *     <li>AND + [...] = assigned every one of the roles, other roles may also be assigned</li>
         *     <li>EXACT + [] = has no roles assigned</li>
         *     <li>EXACT + [...] = assigned every one of the roles and no other roles</li>
         * </ul>
         */
        public enum MatchType {
            OR("or"),
            NOT("not"),
            AND("and"),
            EXACT("exact");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

    /**
     * The message filter of a redblock search request. Filters by the text of a redblock's message.
     */
    public static class MessageFilter {

        // the api names this field "content"
        @SerializedName("content")
        public @NotNull String message;
        public @NotNull MatchType matchType;

        public MessageFilter(@NotNull String message, @NotNull MatchType matchType) {
            this.message = message;
            this.matchType = matchType;
        }

        /**
         * @param text The text to look for
         * @return A filter that only matches redblocks whose message contains the text anywhere
         */
        public static MessageFilter contains(@NotNull String text) {
            return new MessageFilter(text, MatchType.CONTAINS);
        }

        /**
         * @param text The text to match
         * @return A filter that only matches redblocks whose message is exactly the text
         */
        public static MessageFilter exact(@NotNull String text) {
            return new MessageFilter(text, MatchType.EXACT);
        }

        /**
         * @param text The text to look for
         * @return A filter that only matches redblocks whose message starts with the text
         */
        public static MessageFilter startsWith(@NotNull String text) {
            return new MessageFilter(text, MatchType.STARTS_WITH);
        }

        /**
         * @param text The text to look for
         * @return A filter that only matches redblocks whose message ends with the text
         */
        public static MessageFilter endsWith(@NotNull String text) {
            return new MessageFilter(text, MatchType.ENDS_WITH);
        }

        /**
         * How the message text is compared against a redblock's message. All matching ignores case.
         * <ul>
         *     <li>CONTAINS = the message contains the text anywhere</li>
         *     <li>EXACT = the message is exactly the text</li>
         *     <li>STARTS_WITH = the message starts with the text</li>
         *     <li>ENDS_WITH = the message ends with the text</li>
         * </ul>
         */
        public enum MatchType {
            // gson writes enum constant names by default, the api expects these values instead
            @SerializedName("contains") CONTAINS("contains"),
            @SerializedName("exact") EXACT("exact"),
            @SerializedName("startsWith") STARTS_WITH("startsWith"),
            @SerializedName("endsWith") ENDS_WITH("endsWith");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }


    /**
     * The block location to search from
     * @param x The block x coordinate
     * @param y The block y coordinate
     * @param z The block z coordinate
     */
    public record Location(long x, long y, long z) {

        public static Location fromBukkit(org.bukkit.Location location) {
            return new Location(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

    }

}
