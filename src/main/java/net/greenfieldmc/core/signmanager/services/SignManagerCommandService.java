package net.greenfieldmc.core.signmanager.services;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SignManagerEntry;
import net.greenfieldmc.core.signmanager.SignManagerMessages;
import net.greenfieldmc.core.signmanager.arguments.SignEntryNameArgument;
import net.greenfieldmc.core.signmanager.arguments.SignGroupNameArgument;
import net.greenfieldmc.core.signmanager.arguments.SignNameArgument;
import net.greenfieldmc.core.signmanager.paginators.SignManagerPaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;

import java.util.stream.IntStream;

public class SignManagerCommandService extends ModuleService<SignManagerCommandService> implements IModuleService<SignManagerCommandService> {

    private final ISignManagerService signManagerService;
    private final ChatPaginator<SignManagerEntry, ICommandContext> paginator = new SignManagerPaginator().build();

    public SignManagerCommandService(Plugin plugin, Module module, ISignManagerService signManagerService) {
        super(plugin, module);
        this.signManagerService = signManagerService;
    }

    // === User commands ===

    private void listSigns(ICommandContext ctx) throws PDKCommandException {
        var entries = signManagerService.getAllEntries();
        if (entries.isEmpty()) ctx.error("There are no saved signs to display.");
        int page = ctx.getTyped("pageNumber", 1);
        paginator.generatePage(ctx, entries, page).sendTo(ctx.getSender());
    }

    private void searchSigns(ICommandContext ctx) throws PDKCommandException {
        var query = ctx.getTyped("query", String.class);
        var entries = signManagerService.searchEntries(query);
        if (entries.isEmpty()) ctx.error("No signs found matching \"" + query + "\".");
        int page = ctx.getFlag("page", 1);
        paginator.generatePage(ctx, entries, page).sendTo(ctx.getSender());
    }

    private void giveSigns(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var entryName = ctx.getTyped("entryName", String.class);
        var entry = signManagerService.giveEntry(player, entryName);
        if (entry == null) {
            ctx.error(SignManagerMessages.ERROR_ENTRY_NOT_FOUND);
            return;
        }
        if (entry.isGroup()) {
            ctx.send(SignManagerMessages.MODULE.append(
                    Component.text("Gave you " + entry.getSignCount() + " sign(s) from group \"" + entry.getName() + "\".", NamedTextColor.GRAY)));
        } else {
            ctx.send(SignManagerMessages.MODULE.append(
                    Component.text("Gave you sign \"" + entry.getName() + "\".", NamedTextColor.GRAY)));
        }
    }

    // === Staff commands ===

    private void saveSign(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var name = ctx.getTyped("name", String.class);

        if (signManagerService.nameExists(name)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var sign = signManagerService.saveSignFromHand(player, name);
        if (sign == null) {
            ctx.error(SignManagerMessages.ERROR_NOT_HOLDING_SIGN);
            return;
        }
        ctx.send(SignManagerMessages.SIGN_SAVED.apply(name));
    }

    private void deleteSign(ICommandContext ctx) throws PDKCommandException {
        var savedSign = ctx.getTyped("signName", SavedSign.class);
        signManagerService.deleteSign(savedSign.getName());
        ctx.send(SignManagerMessages.SIGN_DELETED.apply(savedSign.getName()));
    }

    private void saveGroup(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var groupName = ctx.getTyped("name", String.class);

        if (signManagerService.nameExists(groupName)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var group = signManagerService.saveGroupFromHotbar(player, groupName);
        if (group == null) {
            ctx.error(SignManagerMessages.ERROR_NO_SIGNS_IN_HOTBAR);
            return;
        }
        ctx.send(SignManagerMessages.GROUP_SAVED.apply(groupName));
        ctx.send(SignManagerMessages.GROUP_SAVED_COUNT.apply(group.getMemberSigns().size()));
    }

    private void deleteGroup(ICommandContext ctx) throws PDKCommandException {
        var groupName = ctx.getTyped("groupName", String.class);
        boolean deleted = signManagerService.deleteGroup(groupName);
        if (!deleted) {
            ctx.error(SignManagerMessages.ERROR_GROUP_NOT_FOUND);
            return;
        }
        ctx.send(SignManagerMessages.GROUP_DELETED.apply(groupName));
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("sm", "signmanager")
                .description("Sign Manager - Save, search, and browse signs")
                .permission("greenfieldcore.signmanager.use")
                .defaultExecutor(this::listSigns)
                .canExecute()
                // User commands
                .then("page")
                    .then("pageNumber", PdkArgumentTypes.integer(ctx -> IntStream.rangeClosed(1, (int) Math.ceil(signManagerService.getAllEntries().size() / 8.0)).boxed().toList(), () -> "Page number")).executes(this::listSigns).end()
                .then("search")
                    .then("query", PdkArgumentTypes.greedyString()).executes(this::searchSigns)
                .end()
                .then("give")
                    .then("entryName", new SignEntryNameArgument(signManagerService)).executes(this::giveSigns)
                .end()
                // Staff commands
                .then("save").permission("greenfieldcore.signmanager.manage")
                    .then("name", PdkArgumentTypes.string()).executes(this::saveSign)
                .end()
                .then("delete").permission("greenfieldcore.signmanager.manage")
                    .then("signName", new SignNameArgument(signManagerService)).executes(this::deleteSign)
                .end()
                .then("savegroup").permission("greenfieldcore.signmanager.manage")
                    .then("name", PdkArgumentTypes.string()).executes(this::saveGroup)
                .end()
                .then("deletegroup").permission("greenfieldcore.signmanager.manage")
                    .then("groupName", new SignGroupNameArgument(signManagerService)).executes(this::deleteGroup)
                .end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
    }
}
