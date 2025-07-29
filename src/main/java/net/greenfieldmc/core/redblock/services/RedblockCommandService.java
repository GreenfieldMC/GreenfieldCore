package net.greenfieldmc.core.redblock.services;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.redblock.arguments.RankArgument;
import net.greenfieldmc.core.shared.arguments.OfflinePlayerArgument;
import net.greenfieldmc.core.redblock.arguments.RedblockArgument;
import net.greenfieldmc.core.redblock.paginators.RedblockInfoPaginator;
import net.greenfieldmc.core.redblock.paginators.RedblockListPaginator;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.greenfieldmc.core.shared.services.IVaultService;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RedblockCommandService extends ModuleService<RedblockCommandService> implements IModuleService<RedblockCommandService> {

    private final IVaultService vaultService;
    private final IRedblockService redblockService;
    private final IEssentialsService essentialsService;

    private final ChatPaginator<Redblock.RedblockInfo, ICommandContext> infoPaginator = new RedblockInfoPaginator().build();
    private final ChatPaginator<Redblock, ICommandContext> listPaginator = new RedblockListPaginator().build();

    public RedblockCommandService(Plugin plugin, Module module, IRedblockService redblockService, IEssentialsService essentialsService, IVaultService vaultService) {
        super(plugin, module);
        this.vaultService = vaultService;
        this.redblockService = redblockService;
        this.essentialsService = essentialsService;
    }

    private void create(ICommandContext ctx) throws PDKCommandException {
        var assignTo = ctx.<UUID>getFlag("assign");
        var rank = ctx.<String>getFlag("rank");
        var description = ctx.getTyped("description", String.class);

        var rb = redblockService.createRedblock(description, ctx.asPlayer(), ctx.getLocation().getBlock().getLocation(), assignTo == null ? null : assignTo, rank);
        ctx.send(RedblockMessages.REDBLOCK_CREATED.apply(rb.getId()));
    }

    private void delete(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, rdb -> rdb.isPending() || rdb.isIncomplete(), "pending or incomplete");
        redblockService.deleteRedblock(rb);
        ctx.send(RedblockMessages.REDBLOCK_DELETED.apply(rb.getId()));
    }

    private void approve(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, Redblock::isPending, "pending");
        redblockService.approveRedblock(rb, ctx.asPlayer());
        ctx.send(RedblockMessages.REDBLOCK_APPROVED.apply(rb.getId()));
    }

    private void deny(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, Redblock::isPending, "pending");
        //if the command sender is the assigned player or if they are the one who completed the redblock, they can deny the completeness of the redblock themselves if they have the permission greenfieldcore.redblock.deny.self
        if (!ctx.hasPermission("greenfieldcore.redblock.deny") && ctx.hasPermission("greenfieldcore.redblock.deny.self")) {
            if ((rb.getAssignedTo() == null || !rb.getAssignedTo().equals(ctx.asPlayer().getUniqueId())) && (rb.getCompletedBy() == null || !rb.getCompletedBy().equals(ctx.asPlayer().getUniqueId()))) {
                ctx.noPermission();
            }
        }

        redblockService.denyRedblock(rb);
        ctx.send(RedblockMessages.REDBLOCK_DENIED.apply(rb.getId()));
    }

    private void complete(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, Redblock::isIncomplete, "incomplete");
        redblockService.completeRedblock(rb, ctx.asPlayer());
        ctx.send(RedblockMessages.REDBLOCK_COMPLETED.apply(rb.getId()));
    }

    private void edit(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, Redblock::isIncomplete, "incomplete");
        var description = ctx.getTyped("description", String.class, "");

        var unassign = ctx.hasFlag("unassign");
        var unrank = ctx.hasFlag("unrank");
        var assignedTo = ctx.<UUID>getFlag("assign");
        var rank = ctx.<String>getFlag("rank");

        if (unassign && assignedTo != null) ctx.error(RedblockMessages.ERROR_EDIT_ASSIGN_UNASSIGN);
        if (unrank && rank != null) ctx.error(RedblockMessages.ERROR_EDIT_RANK_UNRANK);

        if (assignedTo == null && !unassign) assignedTo = rb.getAssignedTo();
        if (rank == null && !unrank) rank = rb.getMinRank();

        redblockService.editRedblock(rb, description.isBlank() ? null : description, assignedTo, rank);
        ctx.send(RedblockMessages.REDBLOCK_EDITED.apply(rb.getId()));
    }

    private void goTo(ICommandContext ctx) throws PDKCommandException {
        if (ctx.hasFlag("id") && ctx.hasFlag("mine")) ctx.error(RedblockMessages.ERROR_GOTO_ID_MINE);

        @SuppressWarnings("DataFlowIssue")
        int radius = ctx.getFlag("radius", ctx.hasFlag("mine") ? -1 : 100);

        var player = ctx.asPlayer();
        var location = ctx.getLocation();
        var rb = ctx.getFlag("id", ctx.hasFlag("mine")
                ? getNearestRedblock(redblockService.getRedblocks(rdb -> rdb.isIncomplete() && rdb.getAssignedTo() != null && rdb.getAssignedTo().equals(player.getUniqueId())), location, radius)
                : getNearestRedblock(redblockService.getRedblocks(Redblock::isIncomplete), ctx.getLocation(), radius));

        if (rb == null) ctx.error(RedblockMessages.ERROR_GOTO_NO_REDBLOCK_FOUND.apply(radius));

        essentialsService.setUserLastLocation(player, location);
        player.teleport(rb.getLocation().clone().add(0.5, 0, 1.5));
        ctx.send(RedblockMessages.REDBLOCK_GOTO.apply(rb.getId()));
    }

    private void info(ICommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, rdb -> rdb.isIncomplete() || rdb.isPending(), "incomplete or pending");

        infoPaginator.generatePage(ctx, rb.getRedblockInfo(ctx.isLocatable() ? ctx.getLocation() : null), 1).sendTo(RedblockMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void list(ICommandContext ctx) throws PDKCommandException {
        var deleted = ctx.hasFlag("deleted");
        var incomplete = ctx.hasFlag("incomplete");
        var pending = ctx.hasFlag("pending");
        var approved = ctx.hasFlag("approved");
        var unassigned = ctx.hasFlag("unassigned");

        var assignedTo = ctx.getFlag("assignedTo", ctx.hasFlag("mine") ? ctx.asPlayer().getUniqueId() : null);
        var createdBy = ctx.<UUID>getFlag("createdBy");
        var completedBy = ctx.<UUID>getFlag("completedBy");
        var approvedBy = ctx.<UUID>getFlag("approvedBy");

        var radius = ctx.getFlag("radius", -1);
        var page = ctx.getFlag("page", 1);

        var filteredStream = redblockService.getRedblocks().stream();

        //if the sender is locatable, we can also do some additional filtering.
        if (ctx.isLocatable()) {
            var location = ctx.getLocation();
            var senderWorldUid = location.getWorld().getUID();

            filteredStream = filteredStream
                    .filter(rb -> rb.getLocation().getWorld().getUID().equals(senderWorldUid))
                    .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceSquared(location)))
                    .filter(rb -> radius == -1 || rb.getLocation().distance(location) <= radius);
        }

        var filtered = filteredStream.filter(rb -> {
                    if (!deleted && !incomplete && !pending && !approved) return true;
                    if (deleted && rb.getStatus() == Redblock.Status.DELETED) return true;
                    if (incomplete && rb.getStatus() == Redblock.Status.INCOMPLETE) return true;
                    if (pending && rb.getStatus() == Redblock.Status.PENDING) return true;
                    return approved && rb.getStatus() == Redblock.Status.APPROVED;
                })
                .filter(rb -> !unassigned || (rb.getAssignedTo() == null && rb.getMinRank() == null))
                .filter(rb -> assignedTo == null || rb.getAssignedTo() != null && rb.getAssignedTo().equals(assignedTo))
                .filter(rb -> createdBy == null || rb.getCreatedBy() != null && rb.getCreatedBy().equals(createdBy))
                .filter(rb -> completedBy == null || rb.getCompletedBy() != null && rb.getCompletedBy().equals(completedBy))
                .filter(rb -> approvedBy == null || rb.getApprovedBy() != null && rb.getApprovedBy().equals(approvedBy))
                .toList();

        listPaginator.generatePage(ctx, filtered, page).sendTo(RedblockMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    // region Helpers
    private Redblock resolveRedblock(ICommandContext ctx, Predicate<Redblock> filter, String type) throws PDKCommandException {
        var rb = ctx.getFlag("id", getNearestRedblock(redblockService.getRedblocks(filter), ctx.getLocation(), 10));
        if (rb == null) {
            ctx.error(ctx.firstOrNull() != null
                    ? RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_ID.apply(type, ctx.first())
                    : RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_RADIUS.apply(type, 10));
        }
        if (!filter.test(rb)) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_ID.apply(type, ctx.first()));
        return rb;
    }

    /**
     * Search through the list of redblocks and return the one that is closest to the given location.
     * @param redblocks The list of redblocks to search through
     * @param location The location to search around
     * @param searchRadius The radius of the search
     * @return The redblock that is closest to the given location, or null if no redblocks are in the list
     */
    private static Redblock getNearestRedblock(List<Redblock> redblocks, Location location, int searchRadius) {
        if (redblocks.isEmpty()) return null;
        Redblock nearestRedblock = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Redblock redblock : redblocks) {
            if (!redblock.getLocation().getWorld().getUID().equals(location.getWorld().getUID())) continue;
            double distance = redblock.getLocation().distance(location);
            if (distance < nearestDistance && (searchRadius == -1 || distance <= searchRadius)) {
                nearestRedblock = redblock;
                nearestDistance = distance;
            }
        }
        return nearestRedblock;
    }

    // endregion

    // region Service Methods

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("rbcreate", "rbc")
                .description("Create a new RedBlock")
                .permission("greenfieldcore.redblock.create")
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Enter a description for the RedBlock"))
                .executes(this::create)
                .flag("assign", "Assign this RedBlock to a specific player", new OfflinePlayerArgument())
                .flag("rank", "Assign this RedBlock to a specific rank",  vaultService != null ? new RankArgument(vaultService) : StringArgumentType.word())
                .register(plugin);

        CommandBuilder.of("rbapprove", "rba")
                .description("Approve a pending RedBlock")
                .permission("greenfieldcore.redblock.approve")
                .flag("id", "The pending RedBlock to approve", new RedblockArgument(redblockService, Redblock::isPending))
                .canExecute(this::approve)
                .register(plugin);

        CommandBuilder.of("rbdeny", "rbd")
                .description("Deny a pending RedBlock")
                .permission("greenfieldcore.redblock.deny", "greenfieldcore.redblock.deny.self")
                .flag("id", "The pending RedBlock to deny", new RedblockArgument(redblockService, Redblock::isPending))
                .canExecute(this::deny)
                .register(plugin);

        CommandBuilder.of("rbcomplete", "rbdone")
                .description("Complete an incomplete RedBlock")
                .permission("greenfieldcore.redblock.complete")
                .flag("id", "The incomplete RedBlock to complete", new RedblockArgument(redblockService, Redblock::isIncomplete))
                .canExecute(this::complete)
                .register(plugin);

        CommandBuilder.of("rbdelete", "rbr", "rbremove", "rbrem", "rbdel")
                .description("Delete an incomplete or pending RedBlock")
                .permission("greenfieldcore.redblock.delete")
                .flag("id", "The RedBlock to delete", new RedblockArgument(redblockService, rb -> rb.isPending() || rb.isIncomplete()))
                .canExecute(this::delete)
                .register(plugin);

        CommandBuilder.of("rbedit", "rbe")
                .description("Edit an incomplete RedBlock")
                .permission("greenfieldcore.redblock.edit")
                .canExecute(this::edit)
                .then("description", PdkArgumentTypes.quotedString(true, () -> "Enter a new description for the RedBlock")).executes(this::edit)
                .flag("id", "The incomplete RedBlock to edit", new RedblockArgument(redblockService, Redblock::isIncomplete))
                .flag("assign", "Assign this RedBlock to a specific player", new OfflinePlayerArgument())
                .flag("unassign", "Unassign this RedBlock")
                .flag("rank", "Assign this RedBlock to a specific rank", vaultService != null ? new RankArgument(vaultService) : StringArgumentType.word())
                .flag("unrank", "Unrank this RedBlock")
                .register(plugin);

        CommandBuilder.of("rbinfo", "rbi")
                .description("View information about a RedBlock")
                .permission("greenfieldcore.redblock.info")
                .flag("id", "The RedBlock to view", new RedblockArgument(redblockService, rb -> true))
                .canExecute(this::info)
                .register(plugin);

        CommandBuilder.of("rbtp", "rbgoto")
                .description("Teleport to an incomplete RedBlock")
                .permission("greenfieldcore.redblock.goto")
                .flag("id", "The RedBlock to teleport to", new RedblockArgument(redblockService, rb -> rb.isIncomplete() || rb.isPending()))
                .flag("mine", "Teleport to the nearest incomplete RedBlock assigned to you")
                .flag("radius", "The radius to search for a RedBlock", PdkArgumentTypes.integer(1, () -> "Specify a radius restriction to conduct the search."))
                .canExecute(this::goTo)
                .register(plugin);

        CommandBuilder.of("rblist", "rbl", "redblocks")
                .description("List all RedBlocks")
                .permission("greenfieldcore.redblock.list")
                .flag("deleted", "Show deleted RedBlocks")
                .flag("incomplete", "Show incomplete RedBlocks")
                .flag("unassigned", "Show unassigned RedBlocks")
                .flag("pending", "Show pending RedBlocks")
                .flag("approved", "Show approved RedBlocks")
                .flag("mine", "Show RedBlocks assigned to you")
                .flag("assignedTo", "Filter who the RedBlock was assigned to", new OfflinePlayerArgument())
                .flag("createdBy", "Filter who the RedBlock was created by", new OfflinePlayerArgument())
                .flag("completedBy", "Filter who the RedBlock was completed by", new OfflinePlayerArgument())
                .flag("approvedBy", "Filter who the RedBlock was approved by", new OfflinePlayerArgument())
                .flag("radius", "Filter RedBlocks within a radius of you", PdkArgumentTypes.integer(1, () -> "Specify a radius restriction to conduct the list search."))
                .flag("page", "The page to view", PdkArgumentTypes.integer(1, () -> "Specify a page number to view."))
                .canExecute(this::list)
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    // endregion

}
