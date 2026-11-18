package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Response body for GET /redblocks/import/bulk/{importJobId}.
 * @param importJobId The id of the import job
 * @param state Queued, Running, Completed or Failed
 * @param total The number of legacy redblocks in the import
 * @param processed The number of legacy redblocks that are done (imported, skipped or failed)
 * @param failed The number of legacy redblocks that failed
 * @param errorMessage Why the whole job failed, if it did
 * @param entries The finished entries after the requested cursor, in order
 * @param cursor The cursor to pass with the next request
 * @param hasMoreEntries Whether more finished entries can be fetched right away with the new cursor
 */
public record GfBulkImportStatus(UUID importJobId,
                                 String state,
                                 int total,
                                 long processed,
                                 long failed,
                                 @Nullable String errorMessage,
                                 List<Entry> entries,
                                 long cursor,
                                 boolean hasMoreEntries) {

    public static final String STATE_COMPLETED = "Completed";
    public static final String STATE_FAILED = "Failed";
    public static final String ENTRY_IMPORTED = "Imported";
    public static final String ENTRY_SKIPPED = "Skipped";

    public boolean isFinished() {
        return STATE_COMPLETED.equals(state) || STATE_FAILED.equals(state);
    }

    /**
     * One processed legacy redblock.
     * @param legacyKey The id of the redblock in redblocks.yml
     * @param state Imported, Skipped or Failed. Importing only appears for jobs that failed while it was being imported
     * @param redblockKey The PROJECT-NUMBER key of the created redblock, or null if none was created
     * @param errors The errors for this redblock. An imported redblock can have errors if a status, assignment or entity failed
     */
    public record Entry(String legacyKey, String state, @Nullable String redblockKey, List<String> errors) {}

}
