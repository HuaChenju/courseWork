package org.example.argregationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.apiclient.ApiClient;
import org.example.apiclient.BibleApiClient;
import org.example.apiclient.GitHubApiClient;
import org.example.apiclient.JikanApiClient;
import org.example.exception.ApiException;
import org.example.storageservice.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AgregationServiceTest {

    AgregationService service;

    @BeforeEach
    void setUp() {
        service = new AgregationService();
    }

    @Test
    void tasks_coolWork() throws ApiException, IOException {
        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.defaultParams()).thenReturn(Map.of());
        when(apiClient.getName()).thenReturn("testApi");
        when(apiClient.getApi(any())).thenReturn(
                new ObjectMapper().readTree("{\"data\":\"ok\"}")
        );

        StorageService storage = mock(StorageService.class);
        ExecutorService mockExecutor = mock(ExecutorService.class);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        service.setExecutor(mockExecutor);

        service.tasks(
                List.of(apiClient),
                "test.json",
                false,
                storage
        );

        verify(storage, times(1))
                .save(anyList(), anyBoolean(), eq("test.json"));

    }

    @Test
    void tasks_shouldNotCallStorageSave_whenApiThrowsException() throws Exception {

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.defaultParams()).thenReturn(Map.of());
        when(apiClient.getName()).thenReturn("testApi");

        when(apiClient.getApi(any()))
                .thenThrow(new ApiException("API error"));

        StorageService storage = mock(StorageService.class);

        ExecutorService mockExecutor = mock(ExecutorService.class);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        service.setExecutor(mockExecutor);

        service.tasks(
                List.of(apiClient),
                "test.json",
                false,
                storage
        );

        verify(storage, never())
                .save(anyList(), anyBoolean(), anyString());
    }

    @Test
    void tasks_shouldHandleStorageException_andNotCrash() throws Exception {

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.defaultParams()).thenReturn(Map.of());
        when(apiClient.getName()).thenReturn("testApi");
        when(apiClient.getApi(any()))
                .thenReturn(new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree("{\"data\":\"ok\"}"));

        StorageService storage = mock(StorageService.class);

        doThrow(new IOException("disk error"))
                .when(storage)
                .save(anyList(), anyBoolean(), anyString());

        ExecutorService mockExecutor = mock(ExecutorService.class);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        service.setExecutor(mockExecutor);

        assertDoesNotThrow(() -> service.tasks(
                List.of(apiClient),
                "test.json",
                false,
                storage
        ));
    }

    @Test
    void tasks_shouldSetAppendFalseFirst_thenTrueNext() throws Exception {

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.defaultParams()).thenReturn(Map.of());
        when(apiClient.getName()).thenReturn("testApi");
        when(apiClient.getApi(any()))
                .thenReturn(new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree("{\"data\":\"ok\"}"));

        StorageService storage = mock(StorageService.class);

        ExecutorService mockExecutor = mock(ExecutorService.class);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        service.setExecutor(mockExecutor);

        service.tasks(List.of(apiClient), "test.json", false, storage);
        service.tasks(List.of(apiClient), "test.json", false, storage);

        verify(storage, times(2))
                .save(anyList(), anyBoolean(), eq("test.json"));

        ArgumentCaptor<Boolean> captor = org.mockito.ArgumentCaptor.forClass(Boolean.class);

        verify(storage, times(2))
                .save(anyList(), captor.capture(), eq("test.json"));

        List<Boolean> values = captor.getAllValues();

        assertEquals(false, values.get(0));
        assertEquals(true, values.get(1));
    }

    @Test
    void buildClients_shouldReturnCorrectClients() {

        List<String> names = List.of("jikan", "bible", "github", "me");

        List<ApiClient> clients = AgregationService.buildClients(names);

        assertEquals(3, clients.size());

        assertInstanceOf(JikanApiClient.class, clients.get(0));
        assertInstanceOf(BibleApiClient.class, clients.get(1));
        assertInstanceOf(GitHubApiClient.class, clients.get(2));
    }

    @Test
    void startPolling_shouldNotStart_whenAlreadyRunning() {

        service.startPolling(1, 1, List.of(), "test.json", false, mock(StorageService.class));

        assertDoesNotThrow(() ->
                service.startPolling(1, 1, List.of(), "test.json", false, mock(StorageService.class))
        );
    }

    @Test
    void stopPolling_shouldShutdownExecutors() {

        AgregationService service = new AgregationService();

        service.startPolling(1, 1, List.of(), "test.json", false, mock(StorageService.class));

        assertDoesNotThrow(service::stopPolling);
    }

}