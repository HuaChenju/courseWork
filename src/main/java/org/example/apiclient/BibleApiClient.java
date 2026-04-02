package org.example.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.ApiException;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleApiClient extends AbstractApiClient {

    @Override
    public String getName() {
        return "bible";
    }

    public BibleApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public JsonNode getApi(Map<String, String> queries) throws ApiException {
        String passage = queries.getOrDefault("passage", "john 3:16");
        String translation = queries.getOrDefault("translation", "web");
        String verseNumbers = queries.getOrDefault("verse_numbers", "true");
        String formatting = queries.getOrDefault("formatting", "plain");
        String includeHeadings = queries.getOrDefault("include_headings", "false");

        String url = "https://bible-api.com/"
                + passage.replace(" ", "%20")
                + "?translation=" + translation
                + "&verse_numbers=" + verseNumbers
                + "&formatting=" + formatting
                + "&include_headings=" + includeHeadings;


        return executeRequest(url);

    }
    @Override
    public Map<String,String> defaultParams() {
        Map<String,String> params = new HashMap<>();
        params.put("passage", "john 2:1-3");
        params.put("translation", "kjv");
        params.put("verse_numbers", "true");
        params.put("formatting", "plain");
        params.put("include_headings", "true");
        return params;
    }
    @Override
    public List<String> getParams() {
        return List.of("passage", "translation", "verse_numbers", "formatting", "include_headings");
    }

}
