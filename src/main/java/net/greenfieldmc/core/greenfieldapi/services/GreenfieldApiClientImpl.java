package net.greenfieldmc.core.greenfieldapi.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class GreenfieldApiClientImpl extends ModuleService<IAuthedClientService> implements IAuthedClientService {

    private static final DateTimeFormatter API_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    private final IGreenfieldApiConfigService configService;
    private final OAuthClientCredentialsHandler authHandler;

    private HttpClient httpClient;
    private Gson gson;
    private String apiUrl;

    public GreenfieldApiClientImpl(Plugin plugin, Module module, IGreenfieldApiConfigService configService, OAuthClientCredentialsHandler authHandler) {
        super(plugin, module);
        this.configService = configService;
        this.authHandler = authHandler;
    }

    @Override
    public <T> CompletableFuture<Result<T>> makeGetRequest(String endpoint, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "GET", null, responseType);
    }

    @Override
    public <T> CompletableFuture<Result<T>> makePostRequest(String endpoint, Object requestBody, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "POST", requestBody, responseType);
    }

    @Override
    public <T> CompletableFuture<Result<T>> makePutRequest(String endpoint, Object requestBody, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "PUT", requestBody, responseType);
    }

    @Override
    public <T> CompletableFuture<Result<T>> makePatchRequest(String endpoint, Object requestBody, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "PATCH", requestBody, responseType);
    }

    @Override
    public <T> CompletableFuture<Result<T>> makeDeleteRequest(String endpoint, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "DELETE", null, responseType);
    }

    @Override
    public <T> CompletableFuture<Result<T>> makeDeleteRequest(String endpoint, Object requestBody, Class<T> responseType) {
        return makeAuthenticatedRequest(endpoint, "DELETE", requestBody, responseType);
    }

    private <T> CompletableFuture<Result<T>> makeAuthenticatedRequest(String endpoint, String method,
                                                                      Object requestBody, Class<T> responseClass) {
        // First, get a valid access token
        return authHandler.getAccessToken().thenCompose(token -> {
            if (token == null) {
                return CompletableFuture.completedFuture(Result.failure("Failed to authenticate with API"));
            }

            // Build the request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + endpoint))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30));

            // Add method and body if applicable
            if (method.equals("GET")) {
                requestBuilder.GET();
            } else if (method.equals("PUT") && requestBody != null) {
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)));
            } else if (method.equals("PATCH") && requestBody != null) {
                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)));
            } else if (method.equals("DELETE")) {
                if (requestBody != null) {
                    requestBuilder.method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)));
                } else {
                    requestBuilder.DELETE();
                }
            } else if (method.equals("POST") && requestBody != null) {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)));
            } else {
                return CompletableFuture.completedFuture(Result.failure("Unsupported HTTP method or missing request body"));
            }

            HttpRequest request = requestBuilder.build();

            // Execute the request asynchronously
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        // Handle 401 - token might be expired, invalidate and retry once
                        if (response.statusCode() == 401) {
                            authHandler.invalidateToken();
                            return Result.<T>failure("Authentication failed - invalid or expired token");
                        }

                        // Handle 404 - resource not found
                        if (response.statusCode() == 404) {
                            return Result.<T>failure("Resource not found");
                        }

                        // Handle other error codes
                        if (response.statusCode() < 200 || response.statusCode() >= 300) {
                            String errorBody = response.body();
                            return Result.<T>failure("API error (" + response.statusCode() + "): " + errorBody);
                        }

                        // Parse successful response
                        String responseJson = response.body();
                        if (responseJson == null || responseJson.isEmpty()) {
                            return Result.<T>failure("Empty response from API");
                        }

                        if (responseClass.equals(String.class)) {
                            String value = responseJson;
                            try {
                                String trimmed = responseJson.trim();
                                if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                                    value = gson.fromJson(responseJson, String.class);
                                }
                            } catch (Exception ignored) {
                                // Fall back to raw response for non-JSON string bodies.
                            }
                            return Result.success(responseClass.cast(value));
                        }

                        try {
                            T data = gson.fromJson(responseJson, responseClass);
                            return Result.success(data);
                        } catch (Exception e) {
                            getModule().getLogger().severe("Failed to parse API response: " + e.getMessage());
                            return Result.<T>failure("Failed to parse response: " + e.getMessage());
                        }
                    })
                    .exceptionally(throwable -> {
                        getModule().getLogger().severe("API request failed: " + throwable.getMessage());
                        return Result.failure("Network error: " + throwable.getMessage());
                    });
        }).exceptionally(throwable -> Result.failure("Authentication error: " + throwable.getMessage()));
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                    if (json == null || json.isJsonNull()) {
                        return null;
                    }
                    String value = json.getAsString();
                    try {
                        return Date.from(OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
                    } catch (DateTimeParseException ignored) {
                        // Fall back to local date-time assumed to be UTC.
                    }
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(value, API_LOCAL_DATE_TIME);
                        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
                    } catch (DateTimeParseException e) {
                        throw new JsonParseException("Unsupported date format: " + value, e);
                    }
                })
                .create();

        this.apiUrl = configService.getApiUrl();

        getModule().getLogger().info("Greenfield Core API client initialized");
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        if (authHandler != null) {
            authHandler.shutdown();
        }
    }
}
