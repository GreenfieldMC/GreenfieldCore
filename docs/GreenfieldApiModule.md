# GreenfieldApi Module

The GreenfieldApi module provides integration with the Greenfield Core API, enabling async communication with the backend API for user and account management.

## Features

- **OAuth2 Authentication**: Automatic client credentials flow authentication with token caching and refresh
- **Async API Calls**: All API requests return `CompletableFuture` for non-blocking operations
- **Result Wrapper**: Clean error handling with `Result<T>` type for success/failure cases
- **User Management**: Get, create, and update user data by Minecraft UUID or internal user ID
- **Account Linking**: Retrieve Discord and Patreon connection information

## Configuration

Configuration file: `greenfield-api.yml`

```yaml
api:
  url: https://dev-api.greenfieldmc.net/scalar/v1
  clientId: your-client-id-here
  clientSecret: your-client-secret-here
```

### Configuration Options

- **api.url**: The base URL of the Greenfield Core API (default: `https://dev-api.greenfieldmc.net/scalar/v1`)
- **api.clientId**: OAuth2 client ID for authentication
- **api.clientSecret**: OAuth2 client secret for authentication

⚠️ **Important**: You must configure valid credentials in the config file. The module will log a warning if using default placeholder values.

## API Endpoints

The module provides access to the following API endpoints:

### User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/user/{minecraftUuid}` | Get user by Minecraft UUID |
| GET | `/user/{userId}` | Get user by internal user ID |
| PUT | `/user/{minecraftUuid}` | Create or update user (full update) |
| PATCH | `/user/{minecraftUuid}` | Update user (partial update) |

### Account Connection Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/user/{userId}/accounts/discord` | Get Discord connection for user |
| GET | `/user/{userId}/accounts/patreon` | Get Patreon connection for user |

## Usage Examples

### Getting the API Service

```java
GreenfieldApiModule apiModule = // get module instance
IGreenfieldCoreApi api = apiModule.getApiService();
```

### Fetching a User by Minecraft UUID

```java
UUID minecraftUuid = player.getUniqueId();

api.getUserByMinecraftUuid(minecraftUuid)
    .thenAccept(result -> {
        result.ifSuccess(user -> {
            // Handle successful response
            getLogger().info("User: " + user.getUsername());
        })
        .ifFailure(error -> {
            // Handle error
            getLogger().warning("Failed to fetch user: " + error);
        });
    });
```

### Fetching a User by Internal ID

```java
long userId = 12345L;

api.getUserById(userId)
    .thenAccept(result -> {
        if (result.isSuccess()) {
            GfUser user = result.getData();
            // Use user data
        } else {
            // Handle error
            String errorMessage = result.getErrorMessage();
        }
    });
```

### Creating or Updating a User

```java
UUID minecraftUuid = player.getUniqueId();
GfUser newUser = new GfUser();
newUser.setMinecraftUuid(minecraftUuid);
newUser.setUsername(player.getName());

api.createOrUpdateUser(minecraftUuid, newUser)
    .thenAccept(result -> {
        result.ifSuccess(user -> {
            getLogger().info("User created/updated: " + user.getUserId());
        })
        .ifFailure(error -> {
            getLogger().severe("Failed to save user: " + error);
        });
    });
```

### Partial User Update (PATCH)

```java
UUID minecraftUuid = player.getUniqueId();
GfUser updates = new GfUser();
updates.setUsername(player.getName()); // Only update username

api.updateUser(minecraftUuid, updates)
    .thenAccept(result -> {
        // Handle result
    });
```

### Getting Discord Connection

```java
long userId = 12345L;

api.getDiscordConnection(userId)
    .thenAccept(result -> {
        result.ifSuccess(connection -> {
            String discordUsername = connection.getDiscordUsername();
            BigInteger snowflake = connection.getDiscordSnowflake();
            getLogger().info("Discord: " + discordUsername);
        });
    });
```

### Getting Patreon Connection

```java
long userId = 12345L;

api.getPatreonConnection(userId)
    .thenAccept(result -> {
        result.ifSuccess(connection -> {
            double pledge = connection.getPledge();
            String name = connection.getFullName();
            getLogger().info("Patreon pledge: $" + pledge);
        });
    });
```

### Chaining API Calls

```java
UUID minecraftUuid = player.getUniqueId();

api.getUserByMinecraftUuid(minecraftUuid)
    .thenCompose(userResult -> {
        if (userResult.isSuccess()) {
            long userId = userResult.getData().getUserId();
            return api.getDiscordConnection(userId);
        } else {
            return CompletableFuture.completedFuture(
                Result.failure("User not found")
            );
        }
    })
    .thenAccept(discordResult -> {
        discordResult.ifSuccess(connection -> {
            // Use Discord connection
        });
    });
```

## Result<T> API

The `Result<T>` wrapper provides convenient methods for handling success and failure cases:

### Methods

- `isSuccess()` - Returns true if the result is successful
- `isFailure()` - Returns true if the result is a failure
- `getData()` - Gets the data (null if failure)
- `getDataOptional()` - Gets the data wrapped in Optional
- `getErrorMessage()` - Gets the error message (null if success)
- `ifSuccess(Consumer<T>)` - Executes consumer if successful (chainable)
- `ifFailure(Consumer<String>)` - Executes consumer if failed (chainable)
- `map(Function<T, U>)` - Maps successful result to new type

### Example: Using Result Methods

```java
api.getUserByMinecraftUuid(uuid)
    .thenAccept(result -> {
        // Chained callbacks
        result.ifSuccess(user -> {
            getLogger().info("Found user: " + user.getUsername());
        })
        .ifFailure(error -> {
            getLogger().warning("Error: " + error);
        });
        
        // Or check manually
        if (result.isSuccess()) {
            GfUser user = result.getData();
        }
        
        // Or use Optional
        result.getDataOptional().ifPresent(user -> {
            // Use user
        });
        
        // Map to different type
        Result<String> usernameResult = result.map(GfUser::getUsername);
    });
```

## Models

### GfUser

Represents a user in the Greenfield system.

**Fields:**
- `long userId` - Unique internal user ID
- `UUID minecraftUuid` - Minecraft UUID
- `String username` - Most recent username
- `Date createdOn` - Account creation date

### GfDiscordConnection

Represents a Discord account connection.

**Fields:**
- `long userDiscordConnectionId` - Connection ID
- `GfUser user` - Associated user
- `long discordConnectionId` - Discord connection ID
- `BigInteger discordSnowflake` - Discord user snowflake ID
- `String discordUsername` - Discord username
- `Date connectedOn` - Connection date
- `Date updatedOn` - Last update date
- `Date createdOn` - Creation date

### GfPatreonConnection

Represents a Patreon account connection.

**Fields:**
- `long userPatreonConnectionId` - Connection ID
- `GfUser user` - Associated user
- `long patreonConnectionId` - Patreon connection ID
- `String fullName` - Patreon full name
- `double pledge` - Current pledge amount
- `Date connectedOn` - Connection date
- `Date updatedOn` - Last update date
- `Date createdOn` - Creation date

## Authentication

The module uses OAuth2 client credentials flow for authentication:

1. On first API call, credentials are sent to `/login/token`
2. Access token is cached with expiration tracking
3. Token is automatically included in all API requests as Bearer token
4. Token is refreshed when expired (60 second buffer before expiry)
5. On 401 response, token is invalidated and must be refreshed

## Error Handling

All API methods return `CompletableFuture<Result<T>>` for consistent error handling:

- **Network errors**: Connection failures, timeouts
- **Authentication errors**: Invalid credentials, expired tokens
- **HTTP errors**: 404 (not found), 401 (unauthorized), etc.
- **Parsing errors**: Invalid JSON responses

Example error handling:

```java
api.getUserByMinecraftUuid(uuid)
    .thenAccept(result -> {
        if (result.isFailure()) {
            String error = result.getErrorMessage();
            // Error messages include context:
            // "Network error: Connection timeout"
            // "Authentication failed - invalid or expired token"
            // "Resource not found"
            // "API error (500): Internal server error"
        }
    })
    .exceptionally(throwable -> {
        // Handle unexpected exceptions
        getLogger().severe("Unexpected error: " + throwable.getMessage());
        return null;
    });
```

## Dependencies

The module uses the following libraries (shaded into the plugin):

- **OkHttp 4.12.0**: HTTP client for async requests
- **Gson 2.10.1**: JSON serialization/deserialization

These are automatically relocated to `net.greenfieldmc.core.libs.*` to avoid conflicts.

## Notes

- All API calls are asynchronous and non-blocking
- Token caching minimizes authentication overhead
- HTTP client connection pooling for efficient resource usage
- Date fields use ISO-8601 format: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
- All services are properly shut down when module disables

