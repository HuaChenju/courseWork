package org.example.apiclient;

import org.example.exception.ApiConnectionException;
import org.example.exception.ApiResponseException;
import org.example.exception.ApiTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractApiClientTest {
    protected HttpClient mockClient;
    protected ApiClient client;

    protected abstract ApiClient createClient(HttpClient client);

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        client = createClient(mockClient);
    }

    @Test
    void getApi_shouldReturnParsedJson_whenStatus200() throws Exception {

        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"result\":\"ok\"}");

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        var result = client.getApi(Map.of());

        assertEquals("ok", result.get("result").asText());
    }

    @Test
    void getApi_shouldThrowException_whenStatusNot200() throws Exception {

        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(404);
        when(response.body()).thenReturn("Not Found");

        when(mockClient.send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertThrows(ApiResponseException.class, () -> client.getApi(Map.of()));
    }

    @Test
    void getApi_shouldThrowTimeoutException_whenTimeout() throws Exception {
        when(mockClient.send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)))
                .thenThrow(new HttpTimeoutException("timeout"));

        assertThrows(ApiTimeoutException.class, () -> client.getApi(Map.of()));
    }

    @Test
    void shouldThrowConnectionException_whenIOException() throws Exception {
        when(mockClient.send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException());

        assertThrows(ApiConnectionException.class, () -> client.getApi(Map.of()));
    }
}
