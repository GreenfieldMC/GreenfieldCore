package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.List;

public class GfRedblockSearchRequest {

    public @Nullable Location location;
    public @Nullable Long radius;
    public @Nullable StatusFilter statusFilter;
    public @Nullable DeletionFilter deletionFilter;
    public @Nullable UserAssignmentFilter userAssignmentFilter;
    public @Nullable RoleAssignmentFilter roleAssignmentFilter;
    public @Nullable MessageFilter messageFilter;

    public int resultsPerPage = 50;
    public long currentPage = 1;

    /**
     * The status filter of a redblock search request. Filters by the LATEST status of a redblock.
     */
    public static class StatusFilter {

        public @NotNull List<String> statuses;
        public @NotNull MatchType matchType;

        public StatusFilter(@NotNull List<String> statuses, @NotNull MatchType matchType) {
            this.statuses = statuses;
            this.matchType = matchType;
        }

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
     * The deletion filter of a redblock search request. Filters by who deleted a redblock.
     */
    public static class DeletionFilter {

        public @NotNull List<Long> users;
        public @NotNull MatchType matchType;

        public DeletionFilter(@NotNull List<Long> users, @NotNull MatchType matchType) {
            this.users = users;
            this.matchType = matchType;
        }

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

    public static class RoleAssignmentFilter {

        public @NotNull List<String> roles;
        public @NotNull MatchType matchType;

        public RoleAssignmentFilter(@NotNull List<String> roles, @NotNull MatchType matchType) {
            this.roles = roles;
            this.matchType = matchType;
        }

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

    public static class MessageFilter {

        public @NotNull String message;
        public @NotNull MatchType matchType;

        public MessageFilter(@NotNull String message, @NotNull MatchType matchType) {
            this.message = message;
            this.matchType = matchType;
        }

        public enum MatchType {
            CONTAINS("contains"),
            EXACT("exact"),
            STARTS_WITH("startsWith"),
            ENDS_WITH("endsWith");

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
     * The location to search from
     * @param x
     * @param y
     * @param z
     */
    public record Location(long x, long y, long z) {

        public static Location fromBukkit(org.bukkit.Location location) {
            return new Location(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

    }

}
