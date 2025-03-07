package com.njdaeger.greenfieldcore.testresult.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.testresult.TestAttempt;
import com.njdaeger.greenfieldcore.testresult.TestSet;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public interface ITestResultService extends IModuleService<ITestResultService> {

    /**
     * Returns the groups that users are added to when they are testing.
     *
     * @return The groups that users are added to when they are testing.
     */
    String getTestingGroup();

    /**
     * Returns the group that users are added to when they pass their testbuild.
     * @return The group that users are added to when they pass their testbuild.
     */
    String getPassingGroup();

    /**
     * Returns the group that users are added to when they fail their testbuild.
     * @return The group that users are added to when they fail their testbuild.
     */
    String getFailingGroup();

    /**
     * Gets the testing information for a user
     * @return The testing information for a user
     */
    List<String> getTestInfo();

    /**
     * Starts a testbuild attempt for a user. Asynchronously.
     * @param whoIsStarting The user starting the attempt
     * @param whoIsStartingThem The user starting the attempt
     * @param callback The callback to run after the attempt is started
     */
    void startAttempt(UUID whoIsStarting, CommandSender whoIsStartingThem, Runnable callback);

    /**
     * Passes a user asynchronously.
     * @param whoIsBeingPassed The user being passed
     * @param whoIsPassingThem The user passing them
     * @param comments The comments for the pass
     * @param callback The callback to run after the user is passed
     */
    void passUser(UUID whoIsBeingPassed, CommandSender whoIsPassingThem, String comments, Runnable callback);

    /**
     * Fails a users testbuild attempt. This does not completely fail the user, it just fails their current attempt and logs it.
     * @param whoIsBeingFailed The user being failed
     * @param whoIsFailingThem The user failing them
     * @param failureReason The reason for the failure
     * @param callback The callback to run after the attempt is failed
     */
    void failAttempt(UUID whoIsBeingFailed, CommandSender whoIsFailingThem, String failureReason, Runnable callback);

    /**
     * Completely fails a user. This will remove them from the testing group and add them to the failing group.
     * @param whoIsBeingFailed The user being failed
     * @param whoIsFailingThem The user failing them
     * @param failureReason The reason for the failure
     * @param callback The callback to run after the user is failed
     */
    void failUser(UUID whoIsBeingFailed, CommandSender whoIsFailingThem, String failureReason, Runnable callback);

    /**
     * Returns the test attempts for a user.
     * @param user The user to get the attempts for
     * @return The test attempts for a user
     */
    List<TestAttempt> getAttemptsForUser(UUID user);

    /**
     * Returns all test sets (a list of all users who have attempts)
     * @return All test sets
     */
    List<TestSet> getAllAttemptSets();

}
