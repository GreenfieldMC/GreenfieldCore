package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.command.brigadier.IAsyncCommandContext;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.greenfieldmc.core.CommonMessages;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfSearchedRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.redblock.arguments.RedblockArgumentV2;
import net.greenfieldmc.core.redblock.arguments.RoleListArgument;
import net.greenfieldmc.core.redblock.arguments.UserListArgument;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import net.greenfieldmc.core.shared.services.IUserService;
import net.greenfieldmc.core.shared.services.IVaultService;
import com.njdaeger.pdk.serviceprovider.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedblockCommandServiceV2 {

    private final IRedblockServiceV2 redblockService;
    private final IUserService userService;
    private final @Nullable IVaultService vaultService;
    private final IRedblockConfigService configService;
    private final @Nullable IEssentialsService essentialsService;

    public RedblockCommandServiceV2(IGreenfieldCorePlugin plugin, IRedblockServiceV2 redblockService, IRedblockConfigService configService, IUserService userService, @Nullable IVaultService vaultService, @Nullable IEssentialsService essentialsService) {
        this.redblockService = redblockService;
        this.configService = configService;
        this.userService = userService;
        this.vaultService = vaultService;
        this.essentialsService = essentialsService;
        registerCommands(plugin);
    }

    private void registerCommands(IGreenfieldCorePlugin plugin) {
        CommandBuilder.ofAsync("rbcreate", "rbc")
                .description("Create a new RedBlock")
                .permission("greenfieldcore.redblock.create")
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Enter a description for the RedBlock"))
                .executes(this::create)
                .flag("assign", "Assign this RedBlock to a group of users", new UserListArgument(userService))
                .flag("role", "Assign this RedBlock to a group of roles", new RoleListArgument(vaultService))
                .register(plugin);

        CommandBuilder.ofAsync("rbdelete", "rbr", "rbremove", "rbrem", "rbdel")
                .description("Delete an incomplete or pending RedBlock")
                .permission("greenfieldcore.redblock.delete")
                .flag("key", "The RedBlock to delete", new RedblockArgumentV2(redblockService, List.of("incomplete", "pending")))
                .canExecute(this::delete)
                .register(plugin);
    }

    // create a new redblock
    private void create(IAsyncCommandContext ctx) throws PDKCommandException {
        var assignUserList = ctx.<List<GfUser>>getFlag("assign");
        var assignRoleList = ctx.<List<String>>getFlag("role");
        var description = ctx.getTyped("description", String.class);

        var rbResult = redblockService.createRedblock(description, ctx.asPlayer(), ctx.getLocation(), assignUserList, assignRoleList).join();
        if (rbResult.isFailure()) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_GENERATED.apply(rbResult.getErrorMessage()));
        ctx.send(RedblockMessages.REDBLOCK_CREATED_NEW.apply(rbResult.getData().getKey()));
    }

    // delete the redblock
    private void delete(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, new GfRedblockSearchRequest.StatusFilter(List.of(IRedblockServiceV2.STATUS_INCOMPLETE, IRedblockServiceV2.STATUS_PENDING), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "pending or incomplete", 10L);
        var deleteResult = redblockService.deleteRedblock(rbKey, ctx.asPlayer()).join();
        if (deleteResult.isFailure()) ctx.error(deleteResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_DELETED_NEW.apply(rbKey));
    }

    // mark the redblock as approved
    private void approve(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, new GfRedblockSearchRequest.StatusFilter(List.of(IRedblockServiceV2.STATUS_PENDING), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "pending", 10L);
        var approveResult = redblockService.approveRedblock(rbKey, ctx.asPlayer()).join();
        if (approveResult.isFailure()) ctx.error(approveResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_APPROVED_NEW.apply(rbKey));
    }

    // mark the redblock as incomplete
    private void deny(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, new GfRedblockSearchRequest.StatusFilter(List.of(IRedblockServiceV2.STATUS_PENDING), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "pending", 10L);
        var denyResult = redblockService.denyRedblock(rbKey, ctx.asPlayer()).join();
        if (denyResult.isFailure()) ctx.error(denyResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_DENIED_NEW.apply(rbKey));
    }

    // mark the redblock as completed
    private void complete(IAsyncCommandContext ctx) throws PDKCommandException {
        var rbKey = resolveRedblockKey(ctx, new GfRedblockSearchRequest.StatusFilter(List.of(IRedblockServiceV2.STATUS_INCOMPLETE), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "incomplete", 10L);
        var completeResult = redblockService.completeRedblock(rbKey, ctx.asPlayer()).join();
        if (completeResult.isFailure()) ctx.error(completeResult.getErrorMessage());
        ctx.send(RedblockMessages.REDBLOCK_COMPLETED_NEW.apply(rbKey));
    }

    private void teleport(IAsyncCommandContext ctx) throws PDKCommandException {
        var goToNearest = ctx.hasFlag("nearest");
        if (goToNearest && ctx.hasFlag("key")) ctx.error(RedblockMessages.ERROR_GOTO_CONFLICTING_FLAGS);

        var rbKey = resolveRedblockKey(ctx, new GfRedblockSearchRequest.StatusFilter(List.of(IRedblockServiceV2.STATUS_INCOMPLETE), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "incomplete", null);

        var redblockResult = redblockService.getRedblock(rbKey).join();
        if (redblockResult.isFailure()) ctx.error(redblockResult.getErrorMessage());
        var redblock = redblockResult.getData();

        var playerLocation = ctx.getLocation();
        var redblockLocation = redblock.getVector().toLocation(playerLocation.getWorld());

        if (essentialsService != null) essentialsService.setUserLastLocation(ctx.asPlayer(), playerLocation);

        ctx.asPlayer().teleport(redblockLocation);
        ctx.send(RedblockMessages.REDBLOCK_GOTO_NEW.apply(redblock.getKey()));
    }

    @NotNull
    private String resolveRedblockKey(ICommandContext ctx, GfRedblockSearchRequest.StatusFilter statusFilter, String typeMessage, Long radius) throws PDKCommandException {
        var rbFlag = ctx.<GfRedblock>getFlag("key");
        if (rbFlag == null && ctx.hasFlag("key"))
            ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_KEY.apply(typeMessage, ctx.getRawFlag("key")));
        if (rbFlag != null) return rbFlag.getKey();

        var search = new GfRedblockSearchRequest();
        search.location = GfRedblockSearchRequest.Location.fromBukkit(ctx.getLocation());
        search.radius = radius;
        search.statusFilter = statusFilter;
        search.resultsPerPage = 1;
        search.deletionFilter = new GfRedblockSearchRequest.DeletionFilter(List.of(), GfRedblockSearchRequest.DeletionFilter.MatchType.AND);

        var searchResult = redblockService.searchRedblocks(ctx.getLocation().getWorld(), search).join();
        if (searchResult.isFailure() || searchResult.getData().getResults().isEmpty())
            ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_RADIUS.apply(typeMessage, Math.toIntExact(radius)));

        return searchResult.getData().getResults().getFirst().getKey();
    }

}
