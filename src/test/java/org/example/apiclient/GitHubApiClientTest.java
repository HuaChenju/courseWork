package org.example.apiclient;

import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GitHubApiClientTest extends AbstractApiClientTest  {
    @Override
    protected ApiClient createClient(HttpClient client) {
        return new GitHubApiClient(client);
    }



    @Test
    void getName() {
        assertEquals("github", client.getName());
    }

    @Test
    void defaultParams() {
        Map<String, String> params = client.defaultParams();

        assertEquals("java", params.get("q"));
        assertEquals("stars", params.get("sort"));
        assertEquals("desc", params.get("order"));
        assertEquals("3", params.get("per_page"));
        assertEquals(4, params.size());
    }

    @Test
    void getParams() {
        assertEquals(List.of("q", "sort", "order", "per_page"), client.getParams());
    }
}