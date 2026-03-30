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
    private void saveEntry(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var signName = ctx.getTyped("name", String.class);

        if (signManagerService.nameExists(signName)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var sign = signManagerService.saveSignFromHand(player, signName);
        if (sign == null) {
            ctx.error(SignManagerMessages.ERROR_NOT_HOLDING_SIGN);
            return;
        }
        ctx.send(SignManagerMessages.SIGN_SAVED.apply(signName));
    }

    private void saveGroupEntry(ICommandContext ctx) throws PDKCommandException {
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

    private void renameSign(ICommandContext ctx) throws PDKCommandException {
        var savedSign = ctx.getTyped("signName", SavedSign.class);
        var newName = ctx.getTyped("newName", String.class);

        if (signManagerService.nameExists(newName)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var oldName = savedSign.getName();
        signManagerService.renameSign(oldName, newName);
        ctx.send(SignManagerMessages.SIGN_RENAMED.apply(oldName, newName));
    }

    /**
     * Unified delete command. Deletes a sign or group by name.
     */
    private void deleteEntry(ICommandContext ctx) throws PDKCommandException {
        var entryName = ctx.getTyped("entryName", String.class);
        boolean deleted = signManagerService.deleteEntry(entryName);
        if (!deleted) {
            ctx.error(SignManagerMessages.ERROR_ENTRY_NOT_FOUND);
            return;
        }
        ctx.send(SignManagerMessages.SIGN_DELETED.apply(entryName));
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
                    .then("pageNumber", PdkArgumentTypes.integer(ctx -> IntStream.rangeClosed(1, (int) Math.ceil(signManagerService.getAllEntries().size() / 8.0)).boxed().toList(), () -> "Page number")).executes(this::listSigns)
                .end()
                .then("search")
                    .then("query", PdkArgumentTypes.greedyString()).executes(this::searchSigns)
                .end()
                .then("give")
                    .then("entryName", new SignEntryNameArgument(signManagerService)).executes(this::giveSigns)
                .end()
                // Staff commands
                .then("save").permission("greenfieldcore.signmanager.manage")
                    .then("-group") // Group save flag
                        .then("name", PdkArgumentTypes.string()).executes(this::saveGroupEntry)
                    .end()
                    .then("name", PdkArgumentTypes.string()).executes(this::saveEntry)
                .end()
                .then("rename").permission("greenfieldcore.signmanager.manage")
                    .then("signName", new SignNameArgument(signManagerService))
                        .then("newName", PdkArgumentTypes.string()).executes(this::renameSign)
                    .end()
                .end()
                .then("delete").permission("greenfieldcore.signmanager.manage")
                    .then("entryName", new SignEntryNameArgument(signManagerService)).executes(this::deleteEntry)
                .end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
    }
}
