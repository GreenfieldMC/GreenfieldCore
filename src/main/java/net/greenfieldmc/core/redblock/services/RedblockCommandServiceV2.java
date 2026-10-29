package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.command.brigadier.IAsyncCommandContext;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.redblock.arguments.RedblockArgumentV2;
import net.greenfieldmc.core.redblock.arguments.RoleListArgument;
import net.greenfieldmc.core.redblock.arguments.UserListArgument;
import net.greenfieldmc.core.shared.services.IUserService;
import net.greenfieldmc.core.shared.services.IVaultService;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedblockCommandServiceV2 {

    private final IRedblockServiceV2 redblockService;
    private final IUserService userService;
    private final @Nullable IVaultService vaultService;

    public RedblockCommandServiceV2(IGreenfieldCorePlugin plugin, IRedblockServiceV2 redblockService, IUserService userService, @Nullable IVaultService vaultService) {
        this.redblockService = redblockService;
        this.userService = userService;
        this.vaultService = vaultService;
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


    private void create(IAsyncCommandContext ctx) throws PDKCommandException {
        var assignUserList = ctx.<List<GfUser>>getFlag("assign");
        var assignRoleList = ctx.<List<String>>getFlag("role");
        var description = ctx.getTyped("description", String.class);

        var rbResult = redblockService.createRedblock(description, ctx.asPlayer(), ctx.getLocation(), assignUserList, assignRoleList).join();
        if (rbResult.isFailure()) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_GENERATED.apply(rbResult.getErrorMessage()));
        ctx.send(RedblockMessages.REDBLOCK_CREATED_NEW.apply(rbResult.getData().getKey()));
    }

    private void delete(IAsyncCommandContext ctx) throws PDKCommandException {
        var rb = resolveRedblock(ctx, new GfRedblockSearchRequest.StatusFilter(List.of("incomplete", "pending"), GfRedblockSearchRequest.StatusFilter.MatchType.OR), "pending or incomplete");
        redblockService.deleteRedblock(rb.getKey(), ctx.asPlayer()).join();
        ctx.send(RedblockMessages.REDBLOCK_DELETED_NEW.apply(rb.getKey()));
    }

    private GfRedblock resolveRedblock(ICommandContext ctx, GfRedblockSearchRequest.StatusFilter statusFilter, String typeMessage) throws PDKCommandException {
        var rb = ctx.<GfRedblock>getFlag("key");
        if (rb != null) return rb;

        if (ctx.hasFlag("key")) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_KEY.apply(typeMessage, ctx.getRawFlag("key")));

        var search = new GfRedblockSearchRequest();
        search.location = GfRedblockSearchRequest.Location.fromBukkit(ctx.getLocation());
        search.radius = 10L;
        search.statusFilter = statusFilter;
        search.resultsPerPage = 1;
        search.deletionFilter = new GfRedblockSearchRequest.DeletionFilter(List.of(), GfRedblockSearchRequest.DeletionFilter.MatchType.AND);

        var searchResult = redblockService.searchRedblocks(search).join();
        if (searchResult.isFailure() || searchResult.getData().isEmpty()) ctx.error(RedblockMessages.ERROR_REDBLOCK_NOT_FOUND_RADIUS.apply(typeMessage, 10));

        return searchResult.getData().get(0);
    }

}
