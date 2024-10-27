package mindsdb.models;

import java.util.Map;

import lombok.Getter;

/**
 * MLEngine class for handling ML engine.
 */
@Getter
public class MLEngine {
    private final String name;
    private final String handler;
    private final Map<String, String> connectionData;

    /**
     * Constructor for MLEngine
     * 
     * @param name           - name of the engine
     * @param handler        - handler of the engine
     * @param connectionData - connection data for the engine
     */
    public MLEngine(String name, String handler, Map<String, String> connectionData) {
        this.name = name;
        this.handler = handler;
        this.connectionData = connectionData;
    }
}