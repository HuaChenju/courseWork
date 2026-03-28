package org.example.storageservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvStorageService implements StorageService {

    public synchronized void save(List<RecordAPI> records, boolean append, String fileName) throws IOException {
        Path path = Path.of(fileName);

        List<Map<String, String>> rows = new ArrayList<>();

        if (append && Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                if (!lines.isEmpty()) {
                    String headerLine = lines.get(0);
                    String[] headers = headerLine.split(",");

                    for (int i = 1; i < lines.size(); i++) {
                        String[] values = lines.get(i).split(",");

                        Map<String, String> row = new LinkedHashMap<>();

                        for (int j = 0; j < headers.length; j++) {
                            row.put(headers[j], j < values.length ? values[j] : "");
                        }

                        rows.add(row);
                    }
                }
        }

        for (RecordAPI record : records) {
            Map<String, String> row = new LinkedHashMap<>();

            row.put("id", String.valueOf(record.id()));
            row.put("source", record.source());
            row.put("timestamp", record.timestamp().toString());
            row.putAll(flatten(record.data(), "data"));
            rows.add(row);
        }

        if (rows.isEmpty()) {
            return;
        }

        Set<String> headersSet = new LinkedHashSet<>();

        for (Map<String, String> row : rows) {
            headersSet.addAll(row.keySet());
        }

        StringBuilder sb = new StringBuilder();
        List<String> headers = new ArrayList<>(headersSet);

        sb.append(String.join(",", headers));
        sb.append("\n");

        for (Map<String, String> row : rows) {

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);

                String value = row.getOrDefault(header, "");

                sb.append(value);

                if (i < headers.size() - 1) {
                    sb.append(",");
                }
            }

            sb.append("\n");
        }

        Files.writeString(path, sb.toString());
    }

    public List<Map<String, String>> readAll(String fileName) throws IOException {
        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + fileName);
        }
        List<String> lines;

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new IOException("error while reading file: " + fileName, e);
        }

        if (lines.isEmpty()) {
            return new ArrayList<>();
        }

        String[] headers = lines.get(0).split(",");
        List<Map<String, String>> result = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {

            String[] values = lines.get(i).split(",");

            Map<String, String> row = new LinkedHashMap<>();

            for (int j = 0; j < headers.length; j++) {
                String value = j < values.length ? values[j] : "";
                row.put(headers[j], value);
            }

            result.add(row);
        }

        return result;

    }

    public List<Map<String, String>> readByApi(String fileName, String sourceName) throws IOException {
        List<Map<String, String>> all = readAll(fileName);
        List<Map<String, String>> filtered = new ArrayList<>();

        for (Map<String, String> row : all) {
            if (sourceName.equals(row.get("source"))) {
                filtered.add(row);
            }
        }

        return filtered;

    }

    private Map<String, String> flatten(JsonNode node, String prefix) {
        Map<String, String> result = new LinkedHashMap<>();

        if (node == null || node.isNull()) {
            return result;
        }

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode childNode = entry.getValue();

                String newPrefix = (prefix == null || prefix.isEmpty())
                        ? fieldName
                        : prefix + "." + fieldName;

                result.putAll(flatten(childNode, newPrefix));
            });
        }


        else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode element = node.get(i);

                String newPrefix = prefix + "[" + i + "]";

                result.putAll(flatten(element, newPrefix));
            }
        }

        else {
            result.put(prefix, node.asText());
        }

        return result;
    }


}
