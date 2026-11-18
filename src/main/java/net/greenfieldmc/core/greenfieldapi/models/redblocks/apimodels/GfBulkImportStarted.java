package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import java.util.UUID;

/**
 * Response body for POST /redblocks/import/bulk. The import runs in the background; follow it with
 * GET /redblocks/import/bulk/{importJobId}.
 * @param importJobId The id of the queued import job
 */
public record GfBulkImportStarted(UUID importJobId) {}
