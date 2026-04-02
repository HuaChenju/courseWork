package org.example.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.ApiException;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JikanApiClient extends AbstractApiClient {


    @Override
    public String getName() {
        return "jikan";
    }

    public JikanApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public JsonNode getApi(Map<String, String> queries) throws ApiException {


        String order_by = queries.getOrDefault("order_by", "score");
        String sort = queries.getOrDefault("sort", "desc");

        String url = "https://api.jikan.moe/v4/anime?order_by=" + order_by + "&sort=" + sort + "&limit=3";

        return executeRequest(url);
    }


    @Override
    public Map<String,String> defaultParams() {
        Map<String,String> params = new HashMap<>();
        params.put("order_by", "score");
        params.put("sort", "desc");
        return params;
    }

    @Override
    public List<String> getParams() {
        return List.of("order_by", "sort");
    }


}