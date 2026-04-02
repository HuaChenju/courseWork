package org.example.storageservice;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvStorageServiceTest {
    CsvStorageService storageService;
    String fileName = "test.csv";

    @BeforeEach
    void setUp() {
        storageService = new CsvStorageService();
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

        List<String> lines = Files.readAllLines(Path.of(fileName));
        assertFalse(lines.isEmpty());
        assertTrue(lines.size() >= 2);

        String dataLine = lines.get(1);
        assertTrue(dataLine.contains("1"));
        assertTrue(dataLine.contains("source"));
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

        List<String> lines = Files.readAllLines(Path.of(fileName));
        assertFalse(lines.isEmpty());
        assertTrue(lines.size() >= 3);

        String dataLine = lines.get(1);
        assertTrue(dataLine.contains("1"));
        dataLine = lines.get(2);
        assertTrue(dataLine.contains("2"));
    }

    @Test
    void readAll_shouldReturnData_whenFileExists() throws Exception {

        RecordAPI record = new RecordAPI(1, "source",  OffsetDateTime.now(),
                new ObjectMapper().readTree("{\"data\":\"ok\"}"));

        storageService.save(List.of(record), false, fileName);


        List<Map<String, String>> lines = storageService.readAll(fileName);

        assertEquals(1, lines.size());
        assertTrue(lines.getFirst().containsKey("source"));
    }

    @Test
    void readAll_shouldReturnEmptyArray_whenFileIsEmpty() throws Exception {
        Files.writeString(Path.of(fileName), "");
        List<Map<String, String>> lines = storageService.readAll(fileName);

        assertEquals(0, lines.size());
    }

    @Test
    void readAll_shouldThrowException_whenFileDoesNotExist() {
        assertThrows(IOException.class, () -> storageService.readAll(fileName));
    }

    @Test
    void readByApi_shouldReturnEmptyArray_whenFileIsEmpty() throws Exception {
        Files.writeString(Path.of(fileName), "");
        List<Map<String, String>> lines = storageService.readByApi(fileName, "source");

        assertEquals(0, lines.size());
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

        List<Map<String, String>> lines = storageService.readByApi(fileName, "source1");

        assertEquals(1, lines.size());
        assertEquals("source1", lines.getFirst().get("source"));
    }

    @Test
    void readByApi_shouldReturnEmpty_whenNoMatches() throws Exception {
        List<RecordAPI> records = List.of(
                new RecordAPI(1, "source1", OffsetDateTime.now(),
                        new ObjectMapper().readTree("{\"data\":\"ok\"}"))
        );

        storageService.save(records, false, fileName);

        List<Map<String, String>> result =
                storageService.readByApi(fileName, "unknown");

        assertEquals(0, result.size());
    }

}