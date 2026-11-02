package net.greenfieldmc.core;

import net.greenfieldmc.core.greenfieldapi.models.Result;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Util {

    public static Map<UUID, String> userNameMap = new ConcurrentHashMap<>();
    public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static String formatDate(long time) {
        return DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(time)));
    }

    public static Map<UUID, String> getAllPlayers() {
        var map = new HashMap<>(Util.userNameMap);

        Bukkit.getWhitelistedPlayers().stream()
                .filter(op -> op.getName() != null)
                .forEach(op -> map.putIfAbsent(op.getUniqueId(), op.getName()));

        Bukkit.getOnlinePlayers().forEach(op -> map.put(op.getUniqueId(), op.getName()));
        return map;
    }

    public static String resolvePlayerName(UUID uuid) {
        var maybePlayer = Bukkit.getPlayer(uuid);
        if (maybePlayer != null) return maybePlayer.getName();
        return getAllPlayers().getOrDefault(uuid, "!!Unknown!!");
    }

    /**
     * Creates an already completed future holding a failed result.
     */
    public static <T> CompletableFuture<Result<T>> failed(String errorMessage) {
        return CompletableFuture.completedFuture(Result.failure(errorMessage));
    }

    /**
     * Runs the next step with the data of a successful result, otherwise passes the failure along. Meant to be passed
     * to {@link CompletableFuture#thenCompose(Function)}.
     */
    public static <T, U> Function<Result<T>, CompletableFuture<Result<U>>> ifSuccess(Function<T, CompletableFuture<Result<U>>> next) {
        return result -> result.isFailure() ? failed(result.getErrorMessage()) : next.apply(result.getData());
    }

    /**
     * Runs the task on the main thread, or immediately if already on it.
     */
    public static <T> CompletableFuture<T> runSync(Plugin plugin, Supplier<T> task) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return CompletableFuture.completedFuture(task.get());
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }
        var future = new CompletableFuture<T>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(task.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
