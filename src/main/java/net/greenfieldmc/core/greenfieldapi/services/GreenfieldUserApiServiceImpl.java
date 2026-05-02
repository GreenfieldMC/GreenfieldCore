package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.greenfieldapi.models.users.GfDiscordConnection;
import net.greenfieldmc.core.greenfieldapi.models.users.GfPatreonConnection;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import org.bukkit.plugin.Plugin;

import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GreenfieldUserApiServiceImpl extends ModuleService<IGreenfieldUserApiService> implements IGreenfieldUserApiService {

    private final IAuthedClientService clientService;
    private final IGreenfieldApiConfigService configService;

    public GreenfieldUserApiServiceImpl(Plugin plugin, Module module, IAuthedClientService clientService, IGreenfieldApiConfigService configService) {
        super(plugin, module);
        this.configService = configService;
        this.clientService = clientService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public CompletableFuture<Result<GfUser>> getUserByMinecraftUuid(UUID minecraftUuid) {
        String endpoint = "/user/" + minecraftUuid.toString();
        return clientService.makeGetRequest(endpoint, GfUser.class);
    }

    @Override
    public CompletableFuture<Result<GfUser>> getUserById(long userId) {
        String endpoint = "/user/" + userId;
        return clientService.makeGetRequest(endpoint, GfUser.class);
    }

    @Override
    public CompletableFuture<Result<GfUser>> createUser(UUID minecraftUuid, String username) {
        String endpoint = "/user/" + minecraftUuid.toString();
        return clientService.makePutRequest(endpoint, new UsernameModel(username), GfUser.class);
    }

    @Override
    public CompletableFuture<Result<GfUser>> updateUser(UUID minecraftUuid, String username) {
        String endpoint = "/user/" + minecraftUuid.toString();
        return clientService.makePatchRequest(endpoint, new UsernameModel(username), GfUser.class);
    }

    @Override
    public CompletableFuture<Result<GfDiscordConnection[]>> getDiscordConnection(long userId) {
        String endpoint = "/user/" + userId + "/accounts/discord";
        return clientService.makeGetRequest(endpoint, GfDiscordConnection[].class);
    }

    @Override
    public CompletableFuture<Result<GfPatreonConnection[]>> getPatreonConnection(long userId) {
        String endpoint = "/user/" + userId + "/accounts/patreon";
        return clientService.makeGetRequest(endpoint, GfPatreonConnection[].class);
    }

    @Override
    public CompletableFuture<Result<String>> getDiscordConnectionLink(long userId) {
        String endpoint = "/discord/oauth/connection-link?userId=" + userId + "&redirectUrl=" + URLEncoder.encode(configService.getRedirectUrl(), java.nio.charset.StandardCharsets.UTF_8);
        return clientService.makeGetRequest(endpoint, String.class);
    }

    @Override
    public CompletableFuture<Result<GfPatreonConnection>> refreshPatreonConnection(long patreonConnectionId) {
        String endpoint = "/patreon/connections/" + patreonConnectionId + "/refresh";
        return clientService.makePostRequest(endpoint, null, GfPatreonConnection.class);
    }

    /**
     * A simple record class to represent the username data for create/update user requests.
     * @param username The username of the user
     */
    private record UsernameModel(String username) {

    }

}
