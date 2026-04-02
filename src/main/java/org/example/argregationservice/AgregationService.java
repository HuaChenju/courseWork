package org.example.argregationservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.ApiException;
import org.example.storageservice.RecordAPI;
import org.example.apiclient.ApiClient;
import org.example.apiclient.BibleApiClient;
import org.example.apiclient.GitHubApiClient;
import org.example.apiclient.JikanApiClient;
import org.example.storageservice.StorageService;


import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class AgregationService {
    private ExecutorService executor;
    private ScheduledExecutorService scheduler;
    private final AtomicBoolean firstWrite = new AtomicBoolean(true);

    public void startPolling(int n, int t, List<String> apiNames, String fileName,
                             boolean append, StorageService storage) {

        if (scheduler != null && !scheduler.isShutdown()) {
            System.err.println("Polling already running");
            return;
        }

        firstWrite.set(true);
        executor = Executors.newFixedThreadPool(n);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        List<ApiClient> clients = buildClients(apiNames);

        scheduler.scheduleWithFixedDelay(
                () -> tasks(clients, fileName, append, storage),
                0,
                t,
                TimeUnit.SECONDS
        );
    }

    void tasks(List<ApiClient> clients, String fileName, boolean append, StorageService storage) {

        for (ApiClient client : clients) {
            Map<String, String> params = client.defaultParams();
            Runnable task = () -> {
                try {

                    JsonNode data = client.getApi(params);

                    RecordAPI record = new RecordAPI(
                            UUID.randomUUID().hashCode(),
                            client.getName(),
                            OffsetDateTime.now(),
                            data
                    );

                    boolean actualAppend = append || !firstWrite.getAndSet(false);
                    storage.save(List.of(record), actualAppend, fileName);


                } catch (ApiException e) {
                    System.err.println("API error [" + client.getName() + "]: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Storage error: " + e.getMessage());
                }
            };
            executor.execute(task);
        }
    }

    public void stopPolling() {

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (executor != null) {
            executor.shutdown();
        }

        try {
            if (scheduler != null && !scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (executor != null && !executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    static List<ApiClient> buildClients(List<String> apiNames) {
        List<ApiClient> clients = new ArrayList<>();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        for (String name : apiNames) {
            switch (name.toLowerCase()) {
                case "jikan" -> clients.add(new JikanApiClient(client));
                case "bible" -> clients.add(new BibleApiClient(client));
                case "github" -> clients.add(new GitHubApiClient(client));
                default -> System.out.println("Unknown API: " + name);
            }
        }

        return clients;


    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

}