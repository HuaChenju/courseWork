package org.example.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.ApiException;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitHubApiClient extends AbstractApiClient {

    @Override
    public String getName() {
        return "github";
    }

    public GitHubApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public JsonNode getApi(Map<String, String> queries) throws ApiException {

        String query = queries.getOrDefault("q", "java");
        String sort = queries.getOrDefault("sort", "stars");
        String order = queries.getOrDefault("order", "desc");
        String perPage = queries.getOrDefault("per_page", "5");

        String url = "https://api.github.com/search/repositories"
                + "?q=" + query
                + "&sort=" + sort
                + "&order=" + order
                + "&per_page=" + perPage;

        return executeRequest(url);
    }

    @Override
    public Map<String,String> defaultParams() {
        Map<String,String> params = new HashMap<>();
        params.put("q", "java");
        params.put("sort", "stars");
        params.put("order", "desc");
        params.put("per_page", "3");
        return params;
    }

    @Override
    public List<String> getParams() {
        return List.of("q", "sort", "order", "per_page");
    }

}