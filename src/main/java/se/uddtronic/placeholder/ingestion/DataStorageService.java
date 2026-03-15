package se.uddtronic.placeholder.ingestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.vaadin.flow.shared.Registration;

@Service
public class DataStorageService {

    private static final int MAX_SIZE = 100;
    private final List<StoredData> storage = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<StorageUpdate>> listeners = new CopyOnWriteArrayList<>();

    public Registration register(Consumer<StorageUpdate> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    private void notifyListeners(StorageUpdate update) {
        listeners.forEach(listener -> listener.accept(update));
    }

    public void storeData(String data, String contentType, String path, Map<String, String> headers, String method,
            Map<String, String[]> queryParameters, int status) {
        var storedData = new StoredData(data, contentType, path, headers, method, queryParameters, status);
        storage.add(storedData);

        StoredData removedItem = null;
        if (storage.size() > MAX_SIZE) {
            removedItem = storage.remove(0);
        }
        notifyListeners(new StorageUpdate(storedData, removedItem));
    }

    public List<StoredData> getAll() {
        return new ArrayList<>(storage);
    }

    public void clear() {
        storage.clear();
        notifyListeners(new StorageUpdate());
    }
}
