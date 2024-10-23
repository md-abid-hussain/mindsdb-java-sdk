package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import mindsdb.models.MLEngine;
import tech.tablesaw.api.Table;

/**
 * MLEngines service class for handling ML engines.
 */
public class MLEngines {
    private final RestAPI api;

    /**
     * Constructor for MLEngines
     * @param api   - RestAPI object
     */
    public MLEngines(RestAPI api) {
        this.api = api;
    }

    private List<MLEngine> _listMLEngines() {
        Table response = api.sqlQuery("SHOW ML_ENGINES;");

        return response.stream().map(row -> new MLEngine(
                row.getString("NAME"),
                row.getString("HANDLER"),
                new JSONObject(row.getString("CONNECTION_DATA"))))
                .collect(Collectors.toList());
    }

    /**
     * List all ML engines
     * @return List of MLEngine objects
     */
    public List<MLEngine> list() {
        return _listMLEngines();
    }

    /**
     * Get a specific ML engine by name
     * @param name  - name of the engine
     * @return MLEngine object
     */
    public MLEngine get(String name) {
        return _listMLEngines().stream()
                .filter(engine -> engine.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("MLEngine doesn't exist"));
    }

    /**
     * Create a new ML engine
     * @param name         - name of the engine
     * @param handler     - handler of the engine
     * @param connectionData - connection data for the engine
     * @return MLEngine object
     */
    public MLEngine create(String name, Handler handler, JSONObject connectionData) {
        return create(name, handler.getName(), connectionData);
    }

    /**
     * Create a new ML engine
     * @param name        - name of the engine
     * @param handlerName - handler of the engine 
     * @param connectionData - connection data for the engine
     * @return MLEngine object
     */
    public MLEngine create(String name, String handlerName, JSONObject connectionData) {
        StringBuilder astQuery = new StringBuilder("CREATE ML_ENGINE IF NOT EXISTS " + name + " FROM " + handlerName);
        if (connectionData != null) {
            astQuery.append(" USING ");
            for (String key : connectionData.keySet()) {
                astQuery.append(key).append("=").append(connectionData.get(key)).append(", ");
            }
            // Remove the trailing comma and space
            astQuery.setLength(astQuery.length() - 2);
        }
        astQuery.append(";");

        api.sqlQuery(astQuery.toString());
        return new MLEngine(name, handlerName, connectionData);
    }

    /**
     * Create a new ML engine
     * @param name  - name of the engine
     * @param handler - handler of the engine
     * @return MLEngine object
     */
    public MLEngine create(String name, Handler handler) {
        return create(name, handler, null);
    }

    /**
     * Create a new ML engine
     * @param name  - name of the engine
     * @param handlerName  - handler of the engine
     * @return  MLEngine object
     */
    public MLEngine create(String name, String handlerName) {
        return create(name, handlerName, null);
    }

    //  TODO: BYOM

    /**
     * Drop an ML engine
     * @param name  - name of the engine
     */
    public void drop(String name) {
        String astQuery = String.format("DROP ML_ENGINE %s;", name);
        api.sqlQuery(astQuery);
    }

}
