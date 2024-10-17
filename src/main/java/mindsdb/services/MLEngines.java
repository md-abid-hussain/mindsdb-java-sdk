package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import mindsdb.models.MLEngine;
import tech.tablesaw.api.Table;

public class MLEngines {
    private final RestAPI api;

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

    public List<MLEngine> list() {
        return _listMLEngines();
    }

    public MLEngine get(String name) {
        return _listMLEngines().stream()
                .filter(engine -> engine.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("MLEngine doesn't exist"));
    }

    public MLEngine create(String name, Handler handler, JSONObject connectionData) {
        return create(name, handler.getName(), connectionData);
    }

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

    public MLEngine create(String name, Handler handler) {
        return create(name, handler, null);
    }

    public MLEngine create(String name, String handlerName) {
        return create(name, handlerName, null);
    }

    // Method to create BYOM with a single requirement
    // public MLEngine createBYOM(String name, String code, String requirements) {
    // return createBYOMEngine(name, code, requirements);
    // }

    // // Method to create BYOM with a list of requirements
    // public MLEngine createBYOM(String name, String code, List<String>
    // requirements) {
    // String requirementsStr = String.join(",", requirements);
    // return createBYOMEngine(name, code, requirementsStr);
    // }

    // // Private method to handle the common logic
    // private MLEngine createBYOMEngine(String name, String code, String
    // requirements) {
    // // Build the query to create the BYOM engine
    // StringBuilder astQuery = new StringBuilder("CREATE BYOM_ENGINE IF NOT EXISTS
    // " + name + " WITH CODE " + code);
    // if (requirements != null && !requirements.isEmpty()) {
    // astQuery.append(" REQUIREMENTS ").append(requirements);
    // }
    // astQuery.append(";");

    // // Execute the query using the API
    // api.sqlQuery(astQuery.toString());

    // // Return the created MLEngine object
    // return new MLEngine(name, code, new JSONObject());
    // }

    public void drop(String name) {
        String astQuery = String.format("DROP ML_ENGINE %s;", name);
        api.sqlQuery(astQuery);
    }

}
