package com.njdaeger.greenfieldcore.testresult.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.testresult.TestAttempt;
import com.njdaeger.greenfieldcore.testresult.TestSet;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestResultStorageServiceImpl extends ModuleService<ITestResultStorageService> implements ITestResultStorageService {

    private IConfig config;
    private final Map<UUID, List<TestAttempt>> attempts = new HashMap<>();

    public TestResultStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "testbuild-storage");
            if (config.hasSection("testsets")) {
                for (var uidString : config.getSection("testsets").getKeys(false)) {
                    var uid = UUID.fromString(uidString);
                    var userAttempts = new ArrayList<TestAttempt>();
                    for (var attempt : config.getSection("testsets").getSection(uidString).getKeys(false)) {
                        var attemptNumber = Integer.parseInt(attempt);
                        var attemptSection = config.getSection("testsets." + uidString + "." + attempt);
                        var attemptStart = attemptSection.getLong("startedOn");
                        var startedBy = UUID.fromString(attemptSection.getString("startedBy"));
                        var attemptEnd = attemptSection.getValue("endedOn") == null ? 0L : attemptSection.getLong("endedOn");
                        var endedBy = attemptSection.getValue("endedBy") == null ? null : UUID.fromString(attemptSection.getString("endedBy"));
                        var comment = attemptSection.getString("comment");
                        var successful = attemptSection.getBoolean("successful");
                        var testAttempt = new TestAttempt(attemptNumber, attemptStart, startedBy, attemptEnd, endedBy, comment, successful);
                        userAttempts.add(testAttempt);
                    }
                    attempts.put(uid, userAttempts);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the TestResultStorageService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
    }

    @Override
    public List<TestSet> getAllAttempts() {
        return attempts.entrySet().stream().map(entry -> new TestSet(entry.getKey(), entry.getValue())).toList();
    }

    @Override
    public List<TestAttempt> getTestAttempts(UUID userId) {
        return attempts.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public TestAttempt getTestAttempt(UUID userId, int attemptNumber) {
        return getTestAttempts(userId).get(attemptNumber);
    }

    @Override
    public void saveTestAttempt(UUID userId, TestAttempt attempt) {
        var currentAttempts = getTestAttempts(userId);
        if (currentAttempts.size() <= attempt.getAttemptNumber()) {
            currentAttempts.add(attempt);
        } else {
            currentAttempts.set(attempt.getAttemptNumber(), attempt);
        }
        attempts.put(userId, currentAttempts);
        var path = "testsets." + userId.toString() + "." + attempt.getAttemptNumber() + ".";
        config.setEntry(path + "startedOn", attempt.getAttemptStart());
        config.setEntry(path + "startedBy", attempt.getStartedBy().toString());
        config.setEntry(path + "endedOn", attempt.getAttemptEnd());
        config.setEntry(path + "endedBy", attempt.getFinishedBy() == null ? null : attempt.getFinishedBy().toString());
        config.setEntry(path + "comment", attempt.getAttemptNotes());
        config.setEntry(path + "successful", attempt.isSuccessful());
    }

    @Override
    public void saveDatabase() {
        attempts.entrySet().stream().filter(entry -> entry.getValue().stream().anyMatch(TestAttempt::hasChanged)).forEach(entry -> {
            var userId = entry.getKey();
            var attempts = entry.getValue();
            for (var attempt : attempts) {
                if (attempt.hasChanged()) {
                    saveTestAttempt(userId, attempt);
                }
            }
        });
        config.save();
    }
}
