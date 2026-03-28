package net.greenfieldmc.core.signmanager.services;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SignFlag;
import net.greenfieldmc.core.signmanager.SignManagerMessages;
import net.greenfieldmc.core.signmanager.arguments.SignFlagArgument;
import net.greenfieldmc.core.signmanager.arguments.SignGroupNameArgument;
import net.greenfieldmc.core.signmanager.arguments.SignNameArgument;
import org.bukkit.plugin.Plugin;

public class SignManagerCommandService extends ModuleService<SignManagerCommandService> implements IModuleService<SignManagerCommandService> {

    private final ISignManagerService signManagerService;
    private final ISignManagerGUIService guiService;

    public SignManagerCommandService(Plugin plugin, Module module, ISignManagerService signManagerService, ISignManagerGUIService guiService) {
        super(plugin, module);
        this.signManagerService = signManagerService;
        this.guiService = guiService;
    }

    // === User commands ===

    private void openBrowser(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        guiService.openGUI(player);
    }

    private void searchSigns(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var query = ctx.getTyped("query", String.class);
        guiService.openGUIWithSearch(player, query);
    }

    private void filterByFlag(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var flag = ctx.getTyped("flag", SignFlag.class);
        guiService.openGUIWithFlag(player, flag);
    }

    // === Staff commands ===

    private void saveSign(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        var name = ctx.getTyped("name", String.class);
        var flag = ctx.getTyped("flag", SignFlag.class, null);

        if (signManagerService.nameExists(name)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var sign = signManagerService.saveSignFromHand(player, name, flag);
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
        var flag = ctx.getTyped("flag", SignFlag.class, null);

        if (signManagerService.nameExists(groupName)) {
            ctx.error(SignManagerMessages.ERROR_SIGN_ALREADY_EXISTS);
            return;
        }

        var group = signManagerService.saveGroupFromHotbar(player, groupName, flag);
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
                .defaultExecutor(this::openBrowser)
                .canExecute()
                // User commands
                .then("search")
                    .then("query", PdkArgumentTypes.greedyString()).executes(this::searchSigns)
                .end()
                .then("flag")
                    .then("flag", new SignFlagArgument()).executes(this::filterByFlag)
                .end()
                // Staff commands
                .then("save").permission("greenfieldcore.signmanager.manage")
                    .then("name", PdkArgumentTypes.string()).canExecute(this::saveSign)
                        .then("flag", new SignFlagArgument()).executes(this::saveSign)
                    .end()
                .end()
                .then("delete").permission("greenfieldcore.signmanager.manage")
                    .then("signName", new SignNameArgument(signManagerService)).executes(this::deleteSign)
                .end()
                .then("savegroup").permission("greenfieldcore.signmanager.manage")
                    .then("name", PdkArgumentTypes.string()).canExecute(this::saveGroup)
                        .then("flag", new SignFlagArgument()).executes(this::saveGroup)
                    .end()
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
