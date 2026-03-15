package se.uddtronic.placeholder.mocks;

import tools.jackson.databind.JsonNode;

public class MockResponse {
    private int status;
    private JsonNode json;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }
}
