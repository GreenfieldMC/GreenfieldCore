package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class TestResultCommands {

    private final Permission permission;
    private final TestResultConfig config;

    public TestResultCommands(GreenfieldCore plugin, TestResultModule module) {
        this.permission = module.getPermissions();
        this.config = module.getConfig();

        plugin.registerCommand(BCIBuilder.create("pass")
                .executor(this::pass)
                .completer((c) -> c.subCompletionAt(0, this::playerCompletion))
                .permissions("greenfieldcore.testresults.pass")
                .description("Pass a test builder")
                .usage("/pass <player>")
                .minArgs(1)
                .maxArgs(1)
                .build()
        );

        plugin.registerCommand(BCIBuilder.create("fail")
                .executor(this::fail)
                .completer((c) -> c.subCompletionAt(0, this::playerCompletion))
                .permissions("greenfieldcore.testresults.fail")
                .description("Fail a test builder")
                .usage("/fail <player>")
                .minArgs(1)
                .maxArgs(1)
                .build()
        );

        plugin.registerCommand(BCIBuilder.create("testresult")
                .aliases("testresults", "result")
                .executor(this::testResult)
                .completer((c) -> {
                    c.subCompletionAt(0, (x) -> x.completion("pass", "fail"));
                    c.subCompletionAt(1, this::playerCompletion);
                })
                .permissions("greenfieldcore.testresults.fail", "greenfieldcore.testresults.pass")
                .description("Pass or fail a test builder")
                .usage("/testresult <pass|fail> <player>")
                .minArgs(2)
                .maxArgs(2)
                .build()
        );
    
        plugin.registerCommand(BCIBuilder.create("testinfo")
            .executor(this::testInfo)
            .permissions("greenfieldcore.testinfo")
            .description("General test build rules.")
            .usage("/testinfo")
            .maxArgs(0)
            .build()
        );

    }

    private void testInfo(CommandContext context) {
        context.send(LIGHT_PURPLE + "[TestInfo] TestBuild rules to follow");
        context.send(LIGHT_PURPLE + "1. " + GRAY + "No hypermodern buildings.");
        context.send(LIGHT_PURPLE + "2. " + GRAY + "Max floor height of a house is 3 floors (excluding basement). Max floor height of a house is 5 floors (excluding basement).");
        context.send(LIGHT_PURPLE + "3. " + GRAY + "Interior must be completed.");
        context.send(LIGHT_PURPLE + "4. " + GRAY + "Codes (/codes) must be followed");
        context.send(LIGHT_PURPLE + "5. " + GRAY + "@ an administrator in Discord when your build is complete.");
        context.send(LIGHT_PURPLE + "6. " + GRAY + "Don't become inactive for several months during a test build.");
        context.send(LIGHT_PURPLE + "7. " + GRAY + "You may build within and on the wooden logs, but do not exceed the plot limit.");
        context.send(LIGHT_PURPLE + "8. " + GRAY + "You may not ask anyone for help nor advice once the test has begun.");
        context.send(LIGHT_PURPLE + "9. " + GRAY + "Please stick to a house, office, or apartment building unless prior arrangements were made.");
    }
    
    private void pass(CommandContext context) throws BCIException {
        Player player = context.argAt(context.getAlias().startsWith("testresult") ? 1 : 0, PlayerParser.class);
        String group = permission.getPrimaryGroup(player);
        if (group == null) throw new BCIException(ChatColor.RED + player.getName() + " is not in a group.");
        if (!config.testingGroups().contains(group)) throw new BCIException(ChatColor.RED + group + " is not a part of the testing groups.");
        permission.playerAddGroup(player, config.passingGroup());
        player.sendMessage(LIGHT_PURPLE + "[TestResult] " + GRAY + "You passed! Welcome to the team!");
        context.send(LIGHT_PURPLE + "[TestResult] " + GRAY + "Passed " + LIGHT_PURPLE + player.getName() + GRAY + ".");
    }

    private void fail(CommandContext context) throws BCIException {
        Player player = context.argAt(context.getAlias().startsWith("testresult") ? 1 : 0, PlayerParser.class);
        String group = permission.getPrimaryGroup(player);
        if (group == null) throw new BCIException(ChatColor.RED + player.getName() + " is not in a group.");
        if (!config.testingGroups().contains(group)) throw new BCIException(ChatColor.RED + group + " is not in any of the following: " + config.testingGroups().toString());
        permission.playerAddGroup(player, config.failingGroup());
        player.sendMessage(LIGHT_PURPLE + "[TestResult] " + GRAY + "We apologize to inform you have failed the test build. Kicking in 10 seconds.");
        context.send(LIGHT_PURPLE + "[TestResult] " + GRAY + "Failed " + LIGHT_PURPLE + player.getName() + GRAY + ".");
        Bukkit.getScheduler().runTaskLater(config.getPlugin(), () -> {
            player.kickPlayer(LIGHT_PURPLE + "[TestResult] " + GRAY + "You can re-apply again to get another chance at joining! We're glad we could meet you.");
            player.setWhitelisted(false);
        }, 200);
    }

    private void testResult(CommandContext context) throws BCIException {
        context.subCommandAt(0, "pass", true, this::pass);
        context.subCommandAt(0, "fail", true, this::fail);
    }


    private void playerCompletion(TabContext context) {
        context.completion((c) -> Bukkit.getOnlinePlayers().stream().map(p -> (Player)p).filter(p -> config.testingGroups().contains(permission.getPrimaryGroup(p))).map(Player::getName).collect(Collectors.toList()));
    }


}
