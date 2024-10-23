package mindsdb.models;

import kong.unirest.core.json.JSONObject;
import lombok.Getter;

@Getter
public class MLEngine {
    private final String name;
    private final String handler;
    private final JSONObject connectionData;

    /**
     * Constructor for MLEngine
     * 
     * @param name           - name of the engine
     * @param handler        - handler of the engine
     * @param connectionData - connection data for the engine
     */
    public MLEngine(String name, String handler, JSONObject connectionData) {
        this.name = name;
        this.handler = handler;
        this.connectionData = connectionData;
    }
}