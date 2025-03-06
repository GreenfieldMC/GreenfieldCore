package com.njdaeger.greenfieldcore.testresult.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.Util;
import com.njdaeger.greenfieldcore.testresult.TestAttempt;
import com.njdaeger.greenfieldcore.testresult.TestResultMessages;
import com.njdaeger.greenfieldcore.testresult.TestSet;
import com.njdaeger.greenfieldcore.testresult.paginators.TestAttemptPaginator;
import com.njdaeger.greenfieldcore.testresult.paginators.TestSetPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class TestResultCommandService extends ModuleService<TestResultCommandService> implements IModuleService<TestResultCommandService> {

    private final ITestResultService testResultService;

    private final ChatPaginator<TestAttempt, ICommandContext> userAttemptPaginator = new TestAttemptPaginator().build();
    private final ChatPaginator<TestSet, ICommandContext> conductedPaginator = new TestSetPaginator().build();

    public TestResultCommandService(Plugin plugin, Module module, ITestResultService testResultService) {
        super(plugin, module);
        this.testResultService = testResultService;
    }

    // /start <user>
    private void start(ICommandContext ctx) throws PDKCommandException {
        var playerBeingStarted = ctx.getTyped("userToStart", UUID.class);
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

    // /attempts user <user> -page <page>
    // This lists out all the attempts a user has taken for a testbuild.
    private void listUserAttempts(ICommandContext ctx) {
        var page = ctx.getFlag("page", 1);
        var user = ctx.getTyped("user", UUID.class);
        var attempts = testResultService.getAttemptsForUser(user);
        userAttemptPaginator.generatePage(ctx, attempts, page).sendTo(TestResultMessages.ERROR_INVALID_PAGE, ctx.getSender());
    }

    // /attempts all -page <page>
    // This lists users and the amount of attempts they have taken in total.
    private void listAllAttempts(ICommandContext ctx) {
        var page = ctx.getFlag("page", 1);
        var attempts = testResultService.getAllAttemptSets();
        conductedPaginator.generatePage(ctx, attempts, page).sendTo(TestResultMessages.ERROR_INVALID_PAGE, ctx.getSender());
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
