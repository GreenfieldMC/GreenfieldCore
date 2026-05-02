package net.greenfieldmc.core.greenfieldapi.models.redblocks;

import java.util.List;

public class GfRedblockSearchResult {

    private List<GfSearchedRedblock> results;

    private long totalResults;

    public GfRedblockSearchResult() {
    }

    public GfRedblockSearchResult(List<GfSearchedRedblock> results, long totalResults) {
        this.results = results;
        this.totalResults = totalResults;
    }

    /**
     * List of redblocks matching the search criteria for the current page.
     * @return list of matching redblocks
     */
    public List<GfSearchedRedblock> getResults() {
        return results;
    }

    /**
     * Count of all results matching the search criteria, regardless of pagination.
     * @return total count of matching results
     */
    public long getTotalResults() {
        return totalResults;
    }

}
