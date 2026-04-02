package org.example.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.exception.ApiConnectionException;
import org.example.exception.ApiException;
import org.example.exception.ApiResponseException;
import org.example.exception.ApiTimeoutException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

public abstract class AbstractApiClient implements ApiClient {

    protected final ObjectMapper mapper;
    protected final HttpClient client;

    protected AbstractApiClient(HttpClient client) {
        this.client = client;
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    protected JsonNode executeRequest(String url) throws ApiException {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiResponseException("HTTP error: " + response.statusCode());
            }

            return mapper.readTree(response.body());

        } catch (HttpTimeoutException e) {
            throw new ApiTimeoutException("Connection timeout", e);

        } catch (IOException e) {
            throw new ApiConnectionException("Network error", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted", e);
        }

    }

}