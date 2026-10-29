package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.shared.services.IDynmapService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static net.greenfieldmc.core.Util.resolvePlayerName;
import static net.greenfieldmc.core.redblock.services.RedblockServiceHelpers.*;

public class RedblockServiceImpl implements IRedblockService {

    private final Logger logger;
    private final @Nullable IDynmapService dynmapService;
    private final IRedblockStorageService storageService;

    public RedblockServiceImpl(Logger logger, @Nullable IDynmapService dynmapService, IRedblockStorageService storageService) {
        this.logger = logger;
        this.dynmapService = dynmapService;
        this.storageService = storageService;
    }

    @Override
    public Redblock getRedblock(int id) {
        return storageService.getRedblock(id);
    }

    @Override
    public List<Redblock> getRedblocks(Predicate<Redblock> filter) {
        return storageService.getRedblocks(filter);
    }

    @Override
    public Redblock createRedblock(String content, Player createdBy, Location location, @Nullable UUID assignedTo, @Nullable String minRank) {
        createCube((assignedTo == null && minRank == null) ? Material.ORANGE_WOOL : Material.RED_WOOL, location, RedblockMessages.SIGN_CLICK_THIS_IF_COMPLETED);
        var displayUuid = createDisplay(storageService.getNextId(), minRank, assignedTo == null ? null : resolvePlayerName(assignedTo), location, content);

        var redblock = new Redblock(storageService.getNextId(), content, Redblock.Status.INCOMPLETE, location, createdBy.getUniqueId(), System.currentTimeMillis(), null, 0, null, 0, assignedTo, assignedTo != null ? System.currentTimeMillis() : 0, minRank, List.of(displayUuid));
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();
        var markerHtml = createMarkerHtml(redblock);
        if (dynmapService != null) dynmapService.tryCreateMarker(RedblockDynmapLoader.INCOMPLETE_MARKER_SET, redblock.getId() + "_redblock", markerHtml, location, RedblockDynmapLoader.INCOMPLETE_MARKER_ICON, true);
        return redblock;
    }

    @Override
    public Redblock editRedblock(Redblock redblock, @Nullable String content, @Nullable UUID assignedTo, @Nullable String minRank) {
        if (content != null && !content.isBlank() && !redblock.getContent().equals(content))
            redblock.setContent(content);

        if (assignedTo == null && redblock.getAssignedTo() != null) {
            redblock.setAssignedTo(null);
            redblock.setAssignedOn(0);
        } else if (assignedTo != null && !assignedTo.equals(redblock.getAssignedTo())) {
            redblock.setAssignedTo(assignedTo);
            redblock.setAssignedOn(System.currentTimeMillis());
        }

        if (minRank == null && redblock.getMinRank() != null) {
            redblock.setMinRank(null);
        } else if (minRank != null && !minRank.equals(redblock.getMinRank())) {
            redblock.setMinRank(minRank);
        }

        removeEntities(redblock.getDisplayEntityIds());
        var displayUuid = createDisplay(redblock.getId(), redblock.getMinRank(), redblock.getAssignedTo() == null ? null : resolvePlayerName(redblock.getAssignedTo()), redblock.getLocation(), redblock.getContent());
        redblock.setDisplayEntityIds(List.of(displayUuid));
        createCube((redblock.getAssignedTo() == null && redblock.getMinRank() == null) ? Material.ORANGE_WOOL : Material.RED_WOOL, redblock.getLocation(), RedblockMessages.SIGN_CLICK_THIS_IF_COMPLETED);
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();
        updateDynmapMarker(redblock);
        return redblock;
    }

    @Override
    public Redblock completeRedblock(Redblock redblock, Player completedBy) {
        redblock.setStatus(Redblock.Status.PENDING);
        redblock.setCompletedBy(completedBy.getUniqueId());
        redblock.setCompletedOn(System.currentTimeMillis());
        createCube(Material.LIME_WOOL, redblock.getLocation(), RedblockMessages.SIGN_CLICK_TO_APPROVE_OR_DENY);
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();
        updateDynmapMarker(redblock);
        return redblock;
    }

    @Override
    public Redblock approveRedblock(Redblock redblock, Player approvedBy) {
        redblock.setStatus(Redblock.Status.APPROVED);
        redblock.setApprovedBy(approvedBy.getUniqueId());
        redblock.setApprovedOn(System.currentTimeMillis());
        redblock.setDisplayEntityIds(removeEntities(redblock.getDisplayEntityIds()));
        createCube(Material.AIR, redblock.getLocation());
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();

        if (dynmapService != null && !dynmapService.tryDeleteMarker(RedblockDynmapLoader.PENDING_MARKER_SET, redblock.getId() + "_redblock")) {
            if (!dynmapService.tryDeleteMarker(RedblockDynmapLoader.INCOMPLETE_MARKER_SET, redblock.getId() + "_redblock"))
                logger.warning("Failed to delete RedBlock marker for #" + redblock.getId());
        }

        return redblock;
    }

    @Override
    public Redblock denyRedblock(Redblock redblock) {
        redblock.setStatus(Redblock.Status.INCOMPLETE);
        redblock.setCompletedBy(null);
        redblock.setCompletedOn(0);
        createCube((redblock.getAssignedTo() == null && redblock.getMinRank() == null) ? Material.ORANGE_WOOL : Material.RED_WOOL, redblock.getLocation(), RedblockMessages.SIGN_CLICK_THIS_IF_COMPLETED);
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();
        updateDynmapMarker(redblock);
        return redblock;
    }

    @Override
    public Redblock deleteRedblock(Redblock redblock) {
        redblock.setStatus(Redblock.Status.DELETED);
        redblock.setDisplayEntityIds(removeEntities(redblock.getDisplayEntityIds()));
        createCube(Material.AIR, redblock.getLocation());
        storageService.saveRedblock(redblock);
        storageService.saveDatabase();

        if (dynmapService != null && !dynmapService.tryDeleteMarker(RedblockDynmapLoader.PENDING_MARKER_SET, redblock.getId() + "_redblock")) {
            if (!dynmapService.tryDeleteMarker(RedblockDynmapLoader.INCOMPLETE_MARKER_SET, redblock.getId() + "_redblock"))
                logger.warning("Failed to delete RedBlock marker for #" + redblock.getId());
        }

        return redblock;
    }

    // region Helper Methods

    private void updateDynmapMarker(Redblock redblock) {
        var markerId = redblock.getId() + "_redblock";
        var set = redblock.isIncomplete() ? RedblockDynmapLoader.INCOMPLETE_MARKER_SET : RedblockDynmapLoader.PENDING_MARKER_SET;
        var icon = redblock.isIncomplete() ? RedblockDynmapLoader.INCOMPLETE_MARKER_ICON : RedblockDynmapLoader.PENDING_MARKER_ICON;
        var markerHtml = createMarkerHtml(redblock);

        if (dynmapService != null && !dynmapService.tryDeleteMarker(RedblockDynmapLoader.INCOMPLETE_MARKER_SET, markerId)) {
            if (!dynmapService.tryDeleteMarker(RedblockDynmapLoader.PENDING_MARKER_SET, markerId))
                logger.warning("Failed to delete RedBlock marker for #" + redblock.getId());
        }

        if (dynmapService != null && !dynmapService.tryCreateMarker(set, redblock.getId() + "_redblock", markerHtml, redblock.getLocation(), icon, true))
            logger.warning("Failed to create RedBlock marker for #" + redblock.getId());
    }

    // endregion

}
