package mindsdb.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Handler;
import mindsdb.utils.DataFrame;

public class Databases {
    private final RestAPI api;

    public Databases(RestAPI api) {
        this.api = api;
    }

    private List<Database> listDatabases() {
        DataFrame response = api.sqlQuery("select NAME, ENGINE from information_schema.databases where TYPE='data'");

        return response.getRows().stream().map(row -> {
            String name = row.get("NAME").toString();
            String engine = row.get("ENGINE").toString();
            return new Database(this.api, name, engine);
        }).collect(Collectors.toList());

    }

    public List<Database> list() {
        return listDatabases();
    }

    public Database get(String name) {
        return listDatabases().stream()
                .filter(database -> database.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database doesn't exist"));
    }

    public Database create(String name, Object engine, Map<String, String> connectionArgs) {

        if (engine == null) {
            throw new IllegalArgumentException("Engine is required");
        }

        if (engine instanceof Handler) {
            engine = ((Handler) engine).getName();
        }
        StringBuilder astQuery = new StringBuilder(
                "CREATE DATABASE " + name + " ENGINE = " + "'" + engine + "'");

        if (connectionArgs != null) {
            astQuery.append(", PARAMETERS = { ");
            for (Map.Entry<String, String> entry : connectionArgs.entrySet()) {
                astQuery.append("\\\"").append(entry.getKey().replace("\"", "\\\"")).append("\\\"")
                        .append(": ")
                        .append("\\\"").append(entry.getValue().replace("\"", "\\\"")).append("\\\"")
                        .append(", ");
            }
            // Remove the trailing comma and space, and close the PARAMETERS object
            astQuery.setLength(astQuery.length() - 2);
            astQuery.append(" }");
        } else {
            astQuery.append(";");
        }

        astQuery = new StringBuilder(astQuery.substring(0, astQuery.length() - 2) + " };");
        api.sqlQuery(astQuery.toString());
        return new Database(this.api, name, engine.toString());
    }

    public void drop(String name) {
        String astQuery = String.format("DROP DATABASE %s", name);
        api.sqlQuery(astQuery);
    }

}
