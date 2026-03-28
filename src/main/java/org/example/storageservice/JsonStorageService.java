package org.example.storageservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageService implements StorageService {
    private final ObjectMapper mapper = new ObjectMapper();

    public synchronized  void save(List<RecordAPI> records, boolean append, String fileName) throws IOException {
        Path path = Path.of(fileName);

        ArrayNode array = mapper.createArrayNode();
        if (append && Files.exists(path)) { // считаю, что если файла нет, то я просто создам новый и в него ток новое запишу
            List<RecordAPI> allRecords = new ArrayList<>();

                String content = Files.readString(path);
                if (!content.isBlank()) {
                    JsonNode root = mapper.readTree(content);
                    if (!root.isArray()) {
                        throw new IOException("JSON root is not an array");
                    }
                    for (JsonNode node : root) {
                        int id = node.get("id").asInt();
                        String source = node.get("source").asText();
                        OffsetDateTime timestamp =
                                OffsetDateTime.parse(node.get("timestamp").asText());
                        JsonNode data = node.get("data");
                        allRecords.add(new RecordAPI(id, source, timestamp, data));
                    }
                }
            array.addAll(getArrayNodeFromRecordApi(allRecords));
            }

        array.addAll(getArrayNodeFromRecordApi(records));
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), array);


    }

    public JsonNode readByApi(String sourceName, String fileName) throws IOException {

        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + fileName);
        }

        String content = Files.readString(path);

        if (content.isBlank()) {
            return mapper.createArrayNode();
        }

        JsonNode root = mapper.readTree(content);

        if (!root.isArray()) {
            throw new IOException("JSON root is not an array");
        }

        ArrayNode filtered = mapper.createArrayNode();

        for (JsonNode node : root) {
            if (sourceName.equals(node.get("source").asText())) {
                filtered.add(node);
            }
        }

        return filtered;
    }

    public JsonNode readAll(String fileName) throws  IOException {
        Path path = Path.of(fileName);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + fileName);
        }

        String content = Files.readString(path);
        if (!content.isBlank()) {
            return mapper.readTree(content);

        } else {
            return mapper.createArrayNode();
        }

    }

    private ArrayNode getArrayNodeFromRecordApi(List<RecordAPI> records){
        ArrayNode array = mapper.createArrayNode();
        ObjectNode objectNode;
        for (RecordAPI record : records) {
            objectNode = mapper.createObjectNode();
            objectNode.put("id", record.id());
            objectNode.put("source", record.source());
            objectNode.put("timestamp", record.timestamp().toString());
            objectNode.set("data", record.data());
            array.add(objectNode);
        }
        return array;
    }
}
