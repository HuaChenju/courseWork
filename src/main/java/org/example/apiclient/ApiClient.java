package org.example.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.ApiException;

import java.util.List;
import java.util.Map;

public interface ApiClient {
    JsonNode getApi(Map<String, String> queries) throws ApiException;
    Map<String,String> defaultParams();
    String getName();
    List<String> getParams();

}
