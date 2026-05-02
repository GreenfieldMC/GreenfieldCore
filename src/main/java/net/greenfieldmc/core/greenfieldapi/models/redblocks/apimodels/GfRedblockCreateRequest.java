package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import java.util.List;

public record GfRedblockCreateRequest(int x, int y, int z, String message, long createdBy, String initialStatus,
                                      List<Long> assignedUsers, List<String> assignedRoles) {

}
