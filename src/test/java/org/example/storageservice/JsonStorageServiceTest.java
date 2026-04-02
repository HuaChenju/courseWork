package org.example.storageservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageServiceTest {
    JsonStorageService storageService;
    String fileName = "test.json";

    @BeforeEach
    public void setup() {
        storageService = new JsonStorageService();
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(Path.of(fileName));
    }

    @Test
    void saveWithFalse() throws IOException {
        List<RecordAPI> records = new ArrayList<>();
        records.add(new RecordAPI(1, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}")));

        storageService.save(records, false, fileName);
        JsonNode result = new ObjectMapper().readTree(Path.of(fileName).toFile());

        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals("source", result.get(0).get("source").asText());
    }

    @Test
    void saveWithTrue() throws IOException {
        List<RecordAPI> records = new ArrayList<>();
        records.add(new RecordAPI(1, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}")));

        storageService.save(records, false, fileName);

        records = new ArrayList<>();
        records.add(new RecordAPI(2, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}")));

        storageService.save(records, true, fileName);

        JsonNode result = new ObjectMapper().readTree(Path.of(fileName).toFile());

        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).get("id").asInt());
        assertEquals(2, result.get(1).get("id").asInt());
    }

    @Test
    void readAll_shouldReturnData_whenFileExists() throws Exception {

        RecordAPI record = new RecordAPI(1, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}"));

        storageService.save(List.of(record), false, fileName);

        JsonNode result = storageService.readAll(fileName);

        assertTrue(result.isArray());
        assertEquals(1, result.size());
    }

    @Test
    void readAll_shouldReturnEmptyArray_whenFileIsEmpty() throws Exception {
        Files.writeString(Path.of(fileName), "");
        JsonNode result = storageService.readAll(fileName);

        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    @Test
    void readAll_shouldThrowException_whenFileDoesNotExist()  {
        assertThrows(IOException.class, () -> storageService.readAll(fileName));
    }


    @Test
    void readByApi_shouldReturnEmptyArray_whenFileIsEmpty() throws Exception {
        Files.writeString(Path.of(fileName), "");
        JsonNode result = storageService.readByApi("source", fileName);

        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    @Test
    void readByApi_shouldThrowException_whenFileDoesNotExist() {
        assertThrows(IOException.class, () -> storageService.readByApi("source", fileName));
    }

    @Test
    void readByApi_shouldReturnData_whenFileExists() throws Exception {
        List<RecordAPI> records = new ArrayList<>();
        records.add(new RecordAPI(1, "source1",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}")));
        records.add(new RecordAPI(2, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}")));
        storageService.save(records, false, fileName);

        JsonNode result = storageService.readByApi("source1", fileName);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
    }

    @Test
    void readByApi_shouldThrowException_whenJsonIsNotArray() throws Exception {

        Files.writeString(Path.of(fileName), "{\"key\":\"value\"}");

        assertThrows(IOException.class, () -> storageService.readByApi("source", fileName));
    }
}