package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockProject;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockRoleAssignment;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockSearchResult;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockStatus;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblockUserAssignment;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfProjectCreateRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockAddStatusRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockCreateRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockDeleteRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockRoleAssignRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockUpdateRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockUserAssignRequest;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GreenfieldRedblockApiServiceImpl extends ModuleService<IGreenfieldRedblockApiService> implements IGreenfieldRedblockApiService {

    private final IAuthedClientService clientService;

    public GreenfieldRedblockApiServiceImpl(Plugin plugin, Module module, IAuthedClientService clientService) {
        super(plugin, module);
        this.clientService = clientService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public CompletableFuture<Result<List<GfRedblockProject>>> getProjects() {
        var endpoint = "/redblocks/projects";
        return clientService.makeGetRequest(endpoint, GfRedblockProject[].class)
                .thenApply(result -> result.map(List::of));
    }

    @Override
    public CompletableFuture<Result<GfRedblockProject>> createProject(String projectKey, String projectName) {
        var endpoint = "/redblocks/projects/" + projectKey;
        return clientService.makePostRequest(endpoint, new GfProjectCreateRequest(projectName), GfRedblockProject.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockProject>> getProject(String projectKey) {
        var endpoint = "/redblocks/projects/" + projectKey;
        return clientService.makeGetRequest(endpoint, GfRedblockProject.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockProject>> updateProject(String projectKey, String projectName) {
        var endpoint = "/redblocks/projects/" + projectKey;
        return clientService.makePutRequest(endpoint, new GfProjectCreateRequest(projectKey), GfRedblockProject.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockSearchResult>> searchRedblocks(String projectKey, GfRedblockSearchRequest searchRequest) {
        var endpoint = "/redblocks/search/" + projectKey;
        return clientService.makePostRequest(endpoint, searchRequest, GfRedblockSearchResult.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> createRedblock(String project, Location location, String message, String initialStatus, long createdBy, List<Long> assignedTo, List<String> assignedRoles) {
        var endpoint = "/redblocks/" + project;
        return clientService.makePostRequest(endpoint, new GfRedblockCreateRequest(location.getBlockX(), location.getBlockY(), location.getBlockZ(), message, createdBy, initialStatus, assignedTo, assignedRoles), GfRedblock.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblock>> getRedblock(String redblockKey) {
        var endpoint = "/redblocks/" + redblockKey;
        return clientService.makeGetRequest(endpoint, GfRedblock.class);
    }

    @Override
    public CompletableFuture<Result<Void>> updateRedblock(String redblockKey, String message, long updatedBy) {
        var endpoint = "/redblocks/" + redblockKey;
        return clientService.makePatchRequest(endpoint, new GfRedblockUpdateRequest(message, updatedBy), Void.class);
    }

    @Override
    public CompletableFuture<Result<List<UUID>>> replaceRedblockEntities(String redblockKey, List<UUID> entities) {
        var endpoint = "/redblocks/" + redblockKey + "/entities";
        return clientService.makePutRequest(endpoint, entities, UUID[].class)
                .thenApply(result -> result.map(List::of));
    }

    @Override
    public CompletableFuture<Result<Void>> clearRedblockEntities(String redblockKey) {
        var endpoint = "/redblocks/" + redblockKey + "/entities";
        return clientService.makeDeleteRequest(endpoint, Void.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockStatus>> addRedblockStatus(String redblockKey, String status, long createdBy) {
        var endpoint = "/redblocks/" + redblockKey + "/statuses";
        return clientService.makePostRequest(endpoint, new GfRedblockAddStatusRequest(status, createdBy), GfRedblockStatus.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockUserAssignment>> addRedblockUserAssignment(String redblockKey, long userId, long assignedBy) {
        var endpoint = "/redblocks/" + redblockKey + "/users";
        return clientService.makePostRequest(endpoint, new GfRedblockUserAssignRequest(userId, assignedBy), GfRedblockUserAssignment.class);
    }

    @Override
    public CompletableFuture<Result<GfRedblockRoleAssignment>> addRedblockRoleAssignment(String redblockKey, String role, long assignedBy) {
        var endpoint = "/redblocks/" + redblockKey + "/roles";
        return clientService.makePostRequest(endpoint, new GfRedblockRoleAssignRequest(role, assignedBy), GfRedblockRoleAssignment.class);
    }

    @Override
    public CompletableFuture<Result<Void>> removeRedblockUserAssignment(String redblockKey, long userId) {
        var endpoint = "/redblocks/" + redblockKey + "/users/" + userId;
        return clientService.makeDeleteRequest(endpoint, Void.class);
    }

    @Override
    public CompletableFuture<Result<Void>> removeRedblockRoleAssignment(String redblockKey, String role) {
        var endpoint = "/redblocks/" + redblockKey + "/roles/" + role;
        return clientService.makeDeleteRequest(endpoint, Void.class);
    }

    @Override
    public CompletableFuture<Result<Void>> deleteRedblock(String redblockKey, long deletedBy) {
        var endpoint = "/redblocks/" + redblockKey;
        return clientService.makeDeleteRequest(endpoint, new GfRedblockDeleteRequest(deletedBy), Void.class);
    }
}
