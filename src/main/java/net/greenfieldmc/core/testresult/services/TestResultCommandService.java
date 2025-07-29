package net.greenfieldmc.core.testresult.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.Util;
import net.greenfieldmc.core.shared.arguments.OfflinePlayerArgument;
import net.greenfieldmc.core.testresult.TestAttempt;
import net.greenfieldmc.core.testresult.TestResultMessages;
import net.greenfieldmc.core.testresult.TestSet;
import net.greenfieldmc.core.testresult.arguments.AttemptingUserArgument;
import net.greenfieldmc.core.testresult.paginators.TestAttemptPaginator;
import net.greenfieldmc.core.testresult.paginators.TestInfoPaginator;
import net.greenfieldmc.core.testresult.paginators.TestSetPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestResultCommandService extends ModuleService<TestResultCommandService> implements IModuleService<TestResultCommandService> {

    private final ITestResultService testResultService;

    private final ChatPaginator<TestAttempt, ICommandContext> userAttemptPaginator = new TestAttemptPaginator().build();
    private final ChatPaginator<TestSet, ICommandContext> conductedPaginator = new TestSetPaginator().build();
    private final ChatPaginator<PageItem<ICommandContext>, ICommandContext> testInfoPaginator = new TestInfoPaginator().build();

    public TestResultCommandService(Plugin plugin, Module module, ITestResultService testResultService) {
        super(plugin, module);
        this.testResultService = testResultService;
    }

    // /start <user>
    private void start(ICommandContext ctx) throws PDKCommandException {
        var playerBeingStarted = ctx.getTyped("userToStart", Player.class).getUniqueId();
        var onlinePlayer = Bukkit.getPlayer(playerBeingStarted);
        if (onlinePlayer == null) ctx.error(TestResultMessages.ERROR_PLAYER_OFFLINE);
        testResultService.startAttempt(playerBeingStarted, ctx.getSender(), () -> {
            onlinePlayer.sendMessage(TestResultMessages.ATTEMPT_START);
            ctx.getSender().sendMessage(TestResultMessages.ATTEMPT_START_SENDER.apply(onlinePlayer.getName()));
        });
    }

    // /pass <user> <comments>
    private void pass(ICommandContext ctx) throws PDKCommandException {
        var playerBeingPassed = ctx.getTyped("userToPass", UUID.class);
        var comments = ctx.getTyped("comments", String.class);
        var onlinePlayer = Bukkit.getPlayer(playerBeingPassed);
        if (onlinePlayer == null) ctx.error(TestResultMessages.ERROR_PLAYER_OFFLINE);
        testResultService.passUser(playerBeingPassed, ctx.getSender(), comments, () -> {
            onlinePlayer.sendMessage(TestResultMessages.ATTEMPT_PASS.apply(onlinePlayer.getName()));
            ctx.getSender().sendMessage(TestResultMessages.ATTEMPT_PASS_SENDER.apply(onlinePlayer.getName()));
        });
    }

    // /fail <user> <comments> -final
    // This will fail a user attempt. if the -final flag is set, the user will be removed from the server.
    private void fail(ICommandContext ctx) {
        var playerBeingFailed = ctx.getTyped("userToFail", UUID.class);
        var comments = ctx.getTyped("comments", String.class);
        var hasFinalFlag = ctx.hasFlag("final");
        if (hasFinalFlag)
            testResultService.failUser(playerBeingFailed, ctx.getSender(), comments, () -> {
                var onlinePlayer = Bukkit.getPlayer(playerBeingFailed);
                if (onlinePlayer != null) onlinePlayer.sendMessage(TestResultMessages.ATTEMPT_FAIL_FINAL);
                ctx.getSender().sendMessage(TestResultMessages.ATTEMPT_FAIL_FINAL_SENDER.apply(onlinePlayer == null ? Util.resolvePlayerName(playerBeingFailed) : onlinePlayer.getName()));
            });
        else
            testResultService.failAttempt(playerBeingFailed, ctx.getSender(), comments, () -> {
                var onlinePlayer = Bukkit.getPlayer(playerBeingFailed);
                if (onlinePlayer != null) onlinePlayer.sendMessage(TestResultMessages.ATTEMPT_FAIL);
                ctx.getSender().sendMessage(TestResultMessages.ATTEMPT_FAIL_SENDER.apply(onlinePlayer == null ? Util.resolvePlayerName(playerBeingFailed) : onlinePlayer.getName()));
            });
    }

    // /attempts <user> -page <page>
    // This lists out all the attempts a user has taken for a testbuild.
    private void listUserAttempts(ICommandContext ctx) {
        var page = ctx.getFlag("page", 1);
        var user = ctx.getTyped("user", UUID.class);
        var attempts = testResultService.getAttemptsForUser(user);
        userAttemptPaginator.generatePage(ctx, attempts, page).sendTo(TestResultMessages.ERROR_INVALID_PAGE, ctx.getSender());
    }

    // /attempts -page <page>
    // This lists users and the amount of attempts they have taken in total.
    private void listAllAttempts(ICommandContext ctx) {
        var page = ctx.getFlag("page", 1);
        var attempts = testResultService.getAllAttemptSets();
        conductedPaginator.generatePage(ctx, attempts, page).sendTo(TestResultMessages.ERROR_INVALID_PAGE, ctx.getSender());
    }

    // /testinfo
    private void testInfo(ICommandContext ctx) {
        var currentItem = new AtomicInteger(1);
        testInfoPaginator.generatePage(ctx, testResultService.getTestInfo().stream().map((info) ->
            new PageItem<ICommandContext>() {
                @Override
                public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
                    return Component.text(currentItem.getAndIncrement() + ". ", paginator.getHighlightColor()).append(Component.text(info, paginator.getGrayColor()));
                }

                @Override
                public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
                    return info;
                }
            }).collect(Collectors.toUnmodifiableList()), 1).sendTo(ctx.getSender());
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("pass")
                .description("Pass a test build attempt.")
                .permission("greenfieldcore.testresult.pass")
                .then("userToPass", new AttemptingUserArgument(testResultService, AttemptingUserArgument.ArgumentMode.INCOMPLETE_ATTEMPTS))
                    .then("comments", PdkArgumentTypes.quotedString(false, () -> "Test attempt notes and comments."))
                    .executes(this::pass)
                .end()
                .register(plugin);

        CommandBuilder.of("fail")
                .description("Fail a test build attempt.")
                .permission("greenfieldcore.testresult.fail")
                .flag("final", "Mark this test build attempt as the final attempt. This will remove the user from the server.")
                .then("userToFail", new AttemptingUserArgument(testResultService, AttemptingUserArgument.ArgumentMode.INCOMPLETE_ATTEMPTS))
                    .then("comments", PdkArgumentTypes.quotedString(false, () -> "Test attempt notes and comments."))
                    .executes(this::fail)
                .end()
                .register(plugin);

        CommandBuilder.of("start")
                .description("Start a test build attempt.")
                .permission("greenfieldcore.testresult.start")
                .then("userToStart", PdkArgumentTypes.player((Predicate<Player>)(p -> p.hasPermission("group." + testResultService.getFailingGroup().toLowerCase()))))
                    .executes(this::start)
                .register(plugin);

        CommandBuilder.of("attempts")
                .description("View test build attempts.")
                .permission("greenfieldcore.testresult.list")
                .flag("page", "The page to view", PdkArgumentTypes.integer(1, Integer.MAX_VALUE))
                .then("user", new AttemptingUserArgument(testResultService, AttemptingUserArgument.ArgumentMode.ANY_ATTEMPTS)).executes(this::listUserAttempts)
                .canExecute(this::listAllAttempts)
                .register(plugin);

        CommandBuilder.of("testinfo")
                .description("View test build information.")
                .permission("greenfieldcore.testinfo")
                .canExecute(this::testInfo)
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
