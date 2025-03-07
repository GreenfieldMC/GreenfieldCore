package com.njdaeger.greenfieldcore.testresult.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.services.IVaultPermissionService;
import com.njdaeger.greenfieldcore.testresult.TestAttempt;
import com.njdaeger.greenfieldcore.testresult.TestSet;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.Util.CONSOLE_UUID;

public class TestResultServiceImpl extends ModuleService<ITestResultService> implements ITestResultService {

    private final ITestResultStorageService storageService;
    private final IVaultPermissionService vaultService;
    private final IConfig config;

    public TestResultServiceImpl(Plugin plugin, Module module, ITestResultStorageService storageService, IVaultPermissionService vaultService) {
        super(plugin, module);
        this.config = ConfigType.YML.createNew(plugin, "testbuild-config");
        this.storageService = storageService;
        this.vaultService = vaultService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {}

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {}

    @Override
    public String getTestingGroup() {
        return config.getString("testing-group");
    }

    @Override
    public String getPassingGroup() {
        return config.getString("passing-group");
    }

    @Override
    public String getFailingGroup() {
        return config.getString("failing-group");
    }

    @Override
    public List<String> getTestInfo() {
        return config.getStringList("test-info");
    }

    @Override
    public void startAttempt(UUID whoIsStarting, CommandSender whoIsStartingThem, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            var attemptCount = storageService.getTestAttempts(whoIsStarting).size();
            var newAttempt = new TestAttempt(attemptCount, System.currentTimeMillis(), resolveUuid(whoIsStartingThem), -1, null, null, false);
            storageService.saveTestAttempt(whoIsStarting, newAttempt);
            vaultService.addUserToGroup(null, whoIsStarting, getTestingGroup()).thenAccept(success -> {
                if (!success) whoIsStartingThem.sendMessage(Component.text("Failed to add the user to the testing group.", NamedTextColor.RED));
                callback.run();
            });
        });
    }

    @Override
    public void passUser(UUID whoIsBeingPassed, CommandSender whoIsPassingThem, String comments, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            var currentAttempt = getIncompleteAttempt(whoIsBeingPassed);
            if (currentAttempt == null) {
                whoIsPassingThem.sendMessage(Component.text("This user has no attempts to be passed.", NamedTextColor.RED));
                return;
            }
            currentAttempt.setAttemptEnd(System.currentTimeMillis());
            currentAttempt.setFinishedBy(resolveUuid(whoIsPassingThem));
            currentAttempt.setAttemptNotes(comments);
            currentAttempt.setSuccessful(true);
            storageService.saveTestAttempt(whoIsBeingPassed, currentAttempt);
            vaultService.addUserToGroup(null, whoIsBeingPassed, getPassingGroup()).thenAccept(success -> {
                if (!success) whoIsPassingThem.sendMessage(Component.text("Failed to add the user to the passing group.", NamedTextColor.RED));
                callback.run();
            });
        });
    }

    @Override
    public void failAttempt(UUID whoIsBeingFailed, CommandSender whoIsFailingThem, String failureReason, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            var currentAttempt = getIncompleteAttempt(whoIsBeingFailed);
            if (currentAttempt == null) {
                whoIsFailingThem.sendMessage(Component.text("This user has no attempts to be failed.", NamedTextColor.RED));
                return;
            }
            currentAttempt.setAttemptEnd(System.currentTimeMillis());
            currentAttempt.setFinishedBy(resolveUuid(whoIsFailingThem));
            currentAttempt.setAttemptNotes(failureReason);
            currentAttempt.setSuccessful(false);
            var newAttempt = new TestAttempt(currentAttempt.getAttemptNumber() + 1, System.currentTimeMillis(), resolveUuid(whoIsFailingThem), -1, null, null, false);
            storageService.saveTestAttempt(whoIsBeingFailed, currentAttempt);
            storageService.saveTestAttempt(whoIsBeingFailed, newAttempt);
            callback.run();
        });
    }

    @Override
    public void failUser(UUID whoIsBeingFailed, CommandSender whoIsFailingThem, String failureReason, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            var currentAttempt = getIncompleteAttempt(whoIsBeingFailed);
            if (currentAttempt == null) {
                whoIsFailingThem.sendMessage(Component.text("This user has no attempts to be failed.", NamedTextColor.RED));
                return;
            }
            currentAttempt.setAttemptEnd(System.currentTimeMillis());
            currentAttempt.setFinishedBy(resolveUuid(whoIsFailingThem));
            currentAttempt.setAttemptNotes(failureReason);
            currentAttempt.setSuccessful(false);
            storageService.saveTestAttempt(whoIsBeingFailed, currentAttempt);
            vaultService.removeUserFromGroup(null, whoIsBeingFailed, getTestingGroup());
            vaultService.addUserToGroup(null, whoIsBeingFailed, getFailingGroup()).thenAccept(success -> {
                if (!success) whoIsFailingThem.sendMessage(Component.text("Failed to remove the user from the failing group.", NamedTextColor.RED));
                callback.run();
            });
        });
    }

    @Override
    public List<TestAttempt> getAttemptsForUser(UUID user) {
        return storageService.getTestAttempts(user);
    }

    @Override
    public List<TestSet> getAllAttemptSets() {
        return storageService.getAllAttempts();
    }

    private static UUID resolveUuid(CommandSender sender) {
        if (sender instanceof Player player) return player.getUniqueId();
        return CONSOLE_UUID;
    }

    private TestAttempt getIncompleteAttempt(UUID user) {
        return storageService.getTestAttempts(user).stream().filter(attempt -> !attempt.isComplete()).findFirst().orElse(null);
    }
}
