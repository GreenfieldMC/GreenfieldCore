package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
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

        CommandBuilder.of("pass")
                .executor(this::pass)
                .completer((c) -> c.subCompletionAt(0, this::playerCompletion))
                .permissions("greenfieldcore.testresults.pass")
                .description("Pass a test builder")
                .usage("/pass <player>")
                .min(1)
                .max(1)
                .build().register(plugin);

        CommandBuilder.of("fail")
                .executor(this::fail)
                .completer((c) -> c.subCompletionAt(0, this::playerCompletion))
                .permissions("greenfieldcore.testresults.fail")
                .description("Fail a test builder")
                .usage("/fail <player>")
                .min(1)
                .max(1)
                .build().register(plugin);
        

        CommandBuilder.of("testresult", "testresults", "result")
                .executor(this::testResult)
                .completer((c) -> {
                    c.subCompletionAt(0, (x) -> x.completion("pass", "fail"));
                    c.subCompletionAt(1, this::playerCompletion);
                })
                .permissions("greenfieldcore.testresults.fail", "greenfieldcore.testresults.pass")
                .description("Pass or fail a test builder")
                .usage("/testresult <pass|fail> <player>")
                .min(2)
                .max(2)
                .build().register(plugin);
    
        CommandBuilder.of("testinfo")
            .executor(this::testInfo)
            .permissions("greenfieldcore.testinfo")
            .description("General test build rules.")
            .usage("/testinfo")
            .max(0)
            .build().register(plugin);

    }

    private void testInfo(CommandContext context) {
        context.send(LIGHT_PURPLE + "[TestInfo] TestBuild rules to follow");
        context.send(LIGHT_PURPLE + "1. " + GRAY + "Absolutely no hypermodern buildings.");
        context.send(LIGHT_PURPLE + "2. " + GRAY + "Maximum number of floors in a house is 3. (Excluding basement)");
        context.send(LIGHT_PURPLE + "3. " + GRAY + "Interior must be completed.");
        context.send(LIGHT_PURPLE + "4. " + GRAY + "All codes (/codes, or acceptance message) must be followed. You may reference these during your test.");
        context.send(LIGHT_PURPLE + "5. " + GRAY + "@ an administrator in Discord when you complete your test build.");
        context.send(LIGHT_PURPLE + "6. " + GRAY + "You have 30 days and 3 attempts to complete your test build. If you take longer than 30 days or fail 3 attempts, you will be remove from the whitelist and will need to reapply.");
        context.send(LIGHT_PURPLE + "7. " + GRAY + "You may build within the wooden logs. Your house cannot sit on the logs, but your fence can.");
        context.send(LIGHT_PURPLE + "8. " + GRAY + "You may not ask anyone for help nor advice once the test has begun.");
        context.send(LIGHT_PURPLE + "9. " + GRAY + "You are to build an American styled house from any era. (Keep it realistic to what you would find in a typical, American, city.");
        context.send(LIGHT_PURPLE + "10. " + GRAY + "You may use a google earth/maps reference for your test build.");
    }
    
    private void pass(CommandContext context) throws PDKCommandException {
        Player player = context.argAt(context.getAlias().startsWith("testresult") ? 1 : 0, PlayerParser.class);
        String group = permission.getPrimaryGroup(player);
        if (group == null) context.error(ChatColor.RED + player.getName() + " is not in a group.");
        if (!config.testingGroups().contains(group)) context.error(ChatColor.RED + group + " is not a part of the testing groups.");
        permission.playerAddGroup(player, config.passingGroup());
        player.sendMessage(LIGHT_PURPLE + "[TestResult] " + GRAY + "You passed! Welcome to the team!");
        context.send(LIGHT_PURPLE + "[TestResult] " + GRAY + "Passed " + LIGHT_PURPLE + player.getName() + GRAY + ".");
    }

    private void fail(CommandContext context) throws PDKCommandException {
        Player player = context.argAt(context.getAlias().startsWith("testresult") ? 1 : 0, PlayerParser.class);
        String group = permission.getPrimaryGroup(player);
        if (group == null) context.error(ChatColor.RED + player.getName() + " is not in a group.");
        if (!config.testingGroups().contains(group)) context.error(ChatColor.RED + group + " is not in any of the following: " + config.testingGroups().toString());
        permission.playerAddGroup(player, config.failingGroup());
        player.sendMessage(LIGHT_PURPLE + "[TestResult] " + GRAY + "We apologize to inform you have failed the test build. Kicking in 10 seconds.");
        context.send(LIGHT_PURPLE + "[TestResult] " + GRAY + "Failed " + LIGHT_PURPLE + player.getName() + GRAY + ".");
        Bukkit.getScheduler().runTaskLater(config.getPlugin(), () -> {
            player.kickPlayer(LIGHT_PURPLE + "[TestResult] " + GRAY + "You can re-apply again to get another chance at joining! We're glad we could meet you.");
            player.setWhitelisted(false);
        }, 200);
    }

    private void testResult(CommandContext context) throws PDKCommandException {
        context.subCommandAt(0, "pass", true, this::pass);
        context.subCommandAt(0, "fail", true, this::fail);
    }


    private void playerCompletion(TabContext context) {
        context.completion((c) -> Bukkit.getOnlinePlayers().stream().map(p -> (Player)p).filter(p -> config.testingGroups().contains(permission.getPrimaryGroup(p))).map(Player::getName).collect(Collectors.toList()));
    }


}
