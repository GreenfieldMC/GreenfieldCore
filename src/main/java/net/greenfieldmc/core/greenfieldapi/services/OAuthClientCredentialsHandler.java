package net.greenfieldmc.core.greenfieldapi.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;


/**
 * Handles OAuth2 client credentials authentication flow for the Greenfield API.
 * Manages access token retrieval, caching, and automatic refresh.
 */
public class OAuthClientCredentialsHandler {

    private final HttpClient httpClient;
    private final Gson gson;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final Logger logger;

    private String accessToken;
    private String tokenType;
    private long tokenExpiresAt; // Unix timestamp in milliseconds

    public OAuthClientCredentialsHandler(String apiUrl, String clientId, String clientSecret, Logger logger) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
        this.tokenUrl = apiUrl + "/login/token";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.logger = logger;
        this.accessToken = null;
        this.tokenType = null;
        this.tokenExpiresAt = 0;
    }

    /**
     * Gets a valid access token, refreshing if necessary.
     * @return A CompletableFuture containing the access token or null if authentication fails
     */
    public CompletableFuture<String> getAccessToken() {
        // Check if we have a valid cached token
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60000) {
            // Token is valid for at least another minute
            return CompletableFuture.completedFuture(accessToken);
        }

        // Need to fetch a new token
        return fetchNewToken();
    }

    /**
     * Fetches a new access token from the authentication server.
     * @return A CompletableFuture containing the new access token or null if authentication fails
     */
    private CompletableFuture<String> fetchNewToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "client_id=" + clientId +
                    "&client_secret=" + URLEncoder.encode(clientSecret, java.nio.charset.StandardCharsets.UTF_8) +
                    "&grant_type=client_credentials"
                ))
                .timeout(Duration.ofSeconds(30))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("Authentication failed with status: " + response.statusCode() + " - " + response.body());
                        return null;
                    }

                    try {
                        String responseJson = response.body();
                        JsonObject tokenResponse = gson.fromJson(responseJson, JsonObject.class);

                        if (tokenResponse.has("access_token")) {
                            accessToken = tokenResponse.get("access_token").getAsString();

                            // Get token type (default to "Bearer" if not provided)
                            tokenType = tokenResponse.has("token_type")
                                    ? tokenResponse.get("token_type").getAsString()
                                    : "Bearer";

                            // Calculate expiration time from expires_in (in seconds)
                            long expiresIn = tokenResponse.has("expires_in")
                                    ? tokenResponse.get("expires_in").getAsLong()
                                    : 3600; // Default to 1 hour
                            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000);

                            logger.info("Successfully authenticated with Greenfield API (token_type: " + tokenType + ", expires_in: " + expiresIn + "s)");
                            return accessToken;
                        } else {
                            logger.severe("Authentication response missing access_token");
                            return null;
                        }
                    } catch (Exception e) {
                        logger.severe("Error parsing authentication response: " + e.getMessage());
                        return null;
                    }
                })
                .exceptionally(e -> {
                    logger.severe("Failed to authenticate with Greenfield API: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Clears the cached access token, forcing a refresh on next request.
     */
    public void invalidateToken() {
        this.accessToken = null;
        this.tokenType = null;
        this.tokenExpiresAt = 0;
    }

    /**
     * Shuts down the HTTP client.
     * Note: Java's built-in HttpClient doesn't require explicit shutdown,
     * but this method is kept for API compatibility.
     */
    public void shutdown() {
        // Java's HttpClient manages its own resources and doesn't require explicit shutdown
        // The executor service is internal and will be cleaned up automatically
    }
}


