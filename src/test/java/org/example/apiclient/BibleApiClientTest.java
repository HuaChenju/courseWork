package org.example.apiclient;

import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BibleApiClientTest extends AbstractApiClientTest {

    @Override
    protected ApiClient createClient(HttpClient client) {
        return new BibleApiClient(client);
    }

    @Test
    void getName() {
        assertEquals("bible", client.getName());
    }

    @Test
    void defaultParams() {
        Map<String,String> params = client.defaultParams();

        assertEquals("john 2:1-3", params.get("passage"));
        assertEquals("kjv", params.get("translation"));
        assertEquals("true", params.get("verse_numbers"));
        assertEquals("plain", params.get("formatting"));
        assertEquals("true", params.get("include_headings"));
    }

    @Test
    void getParams() {
        assertEquals(List.of("passage", "translation", "verse_numbers", "formatting", "include_headings"),
                client.getParams());
    }
}