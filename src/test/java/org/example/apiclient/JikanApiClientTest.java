package org.example.apiclient;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class JikanApiClientTest extends AbstractApiClientTest {

    @Override
    protected ApiClient createClient(HttpClient client) {
        return new JikanApiClient(client);
    }


    @Test
    void defaultParams() {
        Map<String, String> params = client.defaultParams();

        assertEquals("score", params.get("order_by"));
        assertEquals("desc", params.get("sort"));
        assertEquals(2, params.size());
    }

    @Test
    void getName() {
        assertEquals("jikan", client.getName());
    }

    @Test
    void getParams() {
        assertEquals(List.of("order_by", "sort"), client.getParams());
    }



}