package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import java.util.List;
import java.util.UUID;

public record GfRedblockEntitiesRequest(List<UUID> entities) {

    public GfRedblockEntitiesRequest() {
        this(List.of());
    }

}
