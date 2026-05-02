package net.greenfieldmc.core.greenfieldapi.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.greenfieldapi.models.Result;

import java.util.concurrent.CompletableFuture;

public interface IAuthedClientService extends IModuleService<IAuthedClientService> {

    <T> CompletableFuture<Result<T>> makeGetRequest(String endpoint, Class<T> responseType);

    <T> CompletableFuture<Result<T>> makePostRequest(String endpoint, Object requestBody, Class<T> responseType);

    <T> CompletableFuture<Result<T>> makePutRequest(String endpoint, Object requestBody, Class<T> responseType);

    <T> CompletableFuture<Result<T>> makePatchRequest(String endpoint, Object requestBody, Class<T> responseType);

    <T> CompletableFuture<Result<T>> makeDeleteRequest(String endpoint, Class<T> responseType);

    <T> CompletableFuture<Result<T>> makeDeleteRequest(String endpoint, Object requestBody, Class<T> responseType);

}
