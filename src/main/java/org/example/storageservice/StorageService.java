package org.example.storageservice;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    void save(List<RecordAPI> records, boolean append, String fileName) throws IOException;
}
