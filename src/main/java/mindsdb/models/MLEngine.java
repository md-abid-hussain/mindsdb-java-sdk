package mindsdb.models;

import kong.unirest.core.json.JSONObject;

public class MLEngine {
    private String name;
    private String handler;
    private JSONObject connectionData;

    // Constructor
    public MLEngine(String name, String handler, JSONObject connectionData) {
        this.name = name;
        this.handler = handler;
        this.connectionData = connectionData;
    }

    public String getName() {
        return name;
    }
}