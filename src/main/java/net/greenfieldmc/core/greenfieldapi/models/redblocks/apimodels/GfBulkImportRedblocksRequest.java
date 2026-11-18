package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request body for POST /redblocks/import/bulk. Mirrors BulkImportRedblocksRequest in the GreenfieldCoreApi.
 * @param redblocks The legacy redblocks, keyed by their legacy id
 * @param worldProjects The project each world's redblocks are imported into, keyed by world name
 */
public record GfBulkImportRedblocksRequest(Map<String, Entry> redblocks, Map<String, Project> worldProjects) {

    public record Project(String projectName, String projectKey) {}

    /**
     * One legacy redblock. Times are epoch milliseconds, as stored in redblocks.yml.
     */
    public record Entry(String content,
                        String status,
                        UUID createdBy,
                        long createdOn,
                        @Nullable UUID completedBy,
                        @Nullable Long completedOn,
                        @Nullable UUID approvedBy,
                        @Nullable Long approvedOn,
                        @Nullable UUID assignedTo,
                        @Nullable Long assignedOn,
                        Location location,
                        @Nullable String minRank,
                        @Nullable List<UUID> armorstands) {}

    public record Location(long x, long y, long z, String world) {}

}
