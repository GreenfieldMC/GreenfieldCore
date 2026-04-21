package net.greenfieldmc.core.greenfieldapi.models;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A wrapper class for API responses that can either be a success with data or a failure with an error message.
 * @param <T> The type of data contained in a successful result
 */
public class Result<T> {

    private final boolean success;
    private final T data;
    private final String errorMessage;

    protected Result(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful result with the given data.
     * @param data The data to wrap
     * @param <T> The type of the data
     * @return A successful Result containing the data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    /**
     * Creates a failed result with the given error message.
     * @param errorMessage The error message
     * @param <T> The type of the data (unused in failure case)
     * @return A failed Result containing the error message
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage);
    }

    /**
     * Checks if this result represents a success.
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if this result represents a failure.
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Gets the data if this result is successful.
     * @return The data, or null if this is a failure
     */
    public T getData() {
        return data;
    }

    /**
     * Gets the data wrapped in an Optional.
     * @return An Optional containing the data if successful, empty otherwise
     */
    public Optional<T> getDataOptional() {
        return Optional.ofNullable(data);
    }

    /**
     * Gets the error message if this result is a failure.
     * @return The error message, or null if this is a success
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Executes the given consumer if this result is successful.
     * @param consumer The consumer to execute with the data
     * @return This result for chaining
     */
    public Result<T> ifSuccess(Consumer<T> consumer) {
        if (success) {
            consumer.accept(data);
        }
        return this;
    }

    /**
     * Executes the given consumer if this result is a failure.
     * @param consumer The consumer to execute with the error message
     * @return This result for chaining
     */
    public Result<T> ifFailure(Consumer<String> consumer) {
        if (!success) {
            consumer.accept(errorMessage);
        }
        return this;
    }

    /**
     * Maps this result to a new result with a different type if successful.
     * @param mapper The function to map the data
     * @param <U> The new type
     * @return A new Result with the mapped data, or a failure with the same error message
     */
    public <U> Result<U> map(Function<T, U> mapper) {
        if (success && data != null) {
            return Result.success(mapper.apply(data));
        }
        return Result.failure(errorMessage);
    }

    @Override
    public String toString() {
        if (success) {
            return "Result{success=true, data=" + data + "}";
        } else {
            return "Result{success=false, error='" + errorMessage + "'}";
        }
    }
}

