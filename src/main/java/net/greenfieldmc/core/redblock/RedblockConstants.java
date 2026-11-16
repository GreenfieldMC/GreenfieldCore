package net.greenfieldmc.core.redblock;

import java.util.List;

/**
 * Constant values shared across the redblock module.
 */
public final class RedblockConstants {

    private RedblockConstants() {}

    /**
     * A redblock that still needs to be worked on.
     */
    public static final String STATUS_INCOMPLETE = "incomplete";

    /**
     * A redblock that was completed and is waiting for approval.
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * A redblock whose completion was approved.
     */
    public static final String STATUS_APPROVED = "approved";

    /**
     * Every redblock status, in workflow order.
     */
    public static final List<String> STATUSES = List.of(STATUS_INCOMPLETE, STATUS_PENDING, STATUS_APPROVED);

}
