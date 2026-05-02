package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockProject;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockRoleAssignment;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockStatus;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockUserAssignment;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockSearchResult;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IGreenfieldRedblockApiService extends IModuleService<IGreenfieldRedblockApiService> {

    /**
     * Gets a list of all Redblock projects.
     *
     * @return a list of Redblock projects
     */
    CompletableFuture<Result<List<GfRedblockProject>>> getProjects();

    /**
     * Creates a new Redblock project with the given name and key.
     *
     * @param projectKey the key of the project
     * @param projectName the name of the project
     * @return the created Redblock project
     */
    CompletableFuture<Result<GfRedblockProject>> createProject(String projectKey, String projectName);

    CompletableFuture<Result<GfRedblockProject>> getProject(String projectKey);

    CompletableFuture<Result<GfRedblockProject>> updateProject(String projectKey, String projectName);

    CompletableFuture<Result<GfRedblockSearchResult>> searchRedblocks(String projectKey, GfRedblockSearchRequest searchRequest);

    CompletableFuture<Result<GfRedblock>> createRedblock(String project, Location location, String message, String initialStatus, long createdBy, List<Long> assignedTo, List<String> assignedRoles);

    CompletableFuture<Result<GfRedblock>> getRedblock(String redblockKey);

    CompletableFuture<Result<Void>> updateRedblock(String redblockKey, String message, long updatedBy);

    CompletableFuture<Result<List<UUID>>> replaceRedblockEntities(String redblockKey, List<UUID> entities);

    CompletableFuture<Result<Void>> clearRedblockEntities(String redblockKey);

    CompletableFuture<Result<GfRedblockStatus>> addRedblockStatus(String redblockKey, String status, long createdBy);

    CompletableFuture<Result<GfRedblockUserAssignment>> addRedblockUserAssignment(String redblockKey, long userId, long assignedBy);

    CompletableFuture<Result<GfRedblockRoleAssignment>> addRedblockRoleAssignment(String redblockKey, String role, long assignedBy);

    CompletableFuture<Result<Void>> removeRedblockUserAssignment(String redblockKey, long userId);

    CompletableFuture<Result<Void>> removeRedblockRoleAssignment(String redblockKey, String role);

    CompletableFuture<Result<Void>> deleteRedblock(String redblockKey, long deletedBy);

}
