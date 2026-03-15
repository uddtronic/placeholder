package se.uddtronic.placeholder.ingestion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class StoredData {

    private static final AtomicLong ID_COUNTER = new AtomicLong();

    private final Long id;
    private final String path;
    private final String data;
    private final String contentType;
    private final String method;
    private final LocalDateTime createdAt;
    private final Map<String, String> headers;
    private final Map<String, String[]> queryParameters;
    private final int status;

    public StoredData(String data, String contentType, String path, Map<String, String> headers, String method,
            Map<String, String[]> queryParameters, int status) {
        this.id = ID_COUNTER.incrementAndGet();
        this.data = data;
        this.contentType = contentType;
        this.path = path;
        this.createdAt = LocalDateTime.now();
        this.headers = headers;
        this.method = method;
        this.queryParameters = queryParameters;
        this.status = status;
    }


    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }


    public String getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String[]> getQueryParameters() {
        return queryParameters;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StoredData that = (StoredData) obj;
        return Objects.equals(id, that.id);
    }
}
