package org.example.storageservice;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record RecordAPI(
        int id,
        String source,
        OffsetDateTime timestamp,
        JsonNode data
) {}
