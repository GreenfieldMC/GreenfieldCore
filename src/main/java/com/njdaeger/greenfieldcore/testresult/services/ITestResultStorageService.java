package com.njdaeger.greenfieldcore.testresult.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.testresult.TestAttempt;
import com.njdaeger.greenfieldcore.testresult.TestSet;

import java.util.List;
import java.util.UUID;

public interface ITestResultStorageService extends IModuleService<ITestResultStorageService> {

    /**
     * Gets all test attempts for all users.
     * @return A list of test attempts.
     */
    List<TestSet> getAllAttempts();

    /**
     * Gets all test attempts for a user.
     * @param userId The user's UUID.
     * @return A list of test attempts.
     */
    List<TestAttempt> getTestAttempts(UUID userId);

    /**
     * Gets a specific test attempt for a user.
     * @param userId The user's UUID.
     * @param attemptNumber The attempt number.
     * @return The test attempt.
     */
    TestAttempt getTestAttempt(UUID userId, int attemptNumber);

    /**
     * Saves a test attempt for a user to memory, not the database.
     * @param userId The user's UUID.
     * @param attempt The test attempt.
     */
    void saveTestAttempt(UUID userId, TestAttempt attempt);

    /**
     * Saves all changed test attempts to the database
     */
    void saveDatabase();

}
