package mindsdb.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import tech.tablesaw.api.Table;

public class Databases {
    private final RestAPI api;

    public Databases(RestAPI api) {
        this.api = api;
    }

    private List<Database> listDatabases() {
        Table response = api.sqlQuery("select NAME, ENGINE from information_schema.databases where TYPE='data'");

        // return response.getRows().stream().map(row -> {
        // String name = row.get("NAME").toString();
        // String engine = row.get("ENGINE").toString();
        // return new Database(this.api, name, engine);
        // }).collect(Collectors.toList());

        return response.stream().map(row -> {
            String name = row.getString("NAME");
            String engine = row.getString("ENGINE");
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

    // public Database create(String name, Object engine, Map<String, String>
    // connectionArgs) {

    // if (engine == null) {
    // throw new IllegalArgumentException("Engine is required");
    // }

    // if (engine instanceof Handler handler) {
    // engine = handler.getName();
    // }
    // StringBuilder astQuery = new StringBuilder(
    // "CREATE DATABASE " + name + " ENGINE = " + "'" + engine + "'");

    // if (connectionArgs != null && !connectionArgs.isEmpty()) {
    // astQuery.append(", PARAMETERS = { ");
    // for (Map.Entry<String, String> entry : connectionArgs.entrySet()) {
    // astQuery.append("\\\"").append(entry.getKey().replace("\"",
    // "\\\"")).append("\\\"")
    // .append(": ")
    // .append("\\\"").append(entry.getValue().replace("\"", "\\\"")).append("\\\"")
    // .append(", ");
    // }
    // // Remove the trailing comma and space, and close the PARAMETERS object
    // astQuery.setLength(astQuery.length() - 2);
    // astQuery.append(" }");
    // }
    // astQuery = new StringBuilder(astQuery + ";");
    // api.sqlQuery(astQuery.toString());
    // return new Database(this.api, name, engine.toString());
    // }

    public Database create(String name, String engine, Map<String, String> connectionArgs) {

        StringBuilder astQuery = new StringBuilder(
                "CREATE DATABASE " + name + " ENGINE = " + "'" + engine + "'");

        if (connectionArgs != null && !connectionArgs.isEmpty()) {
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
        }
        astQuery = new StringBuilder(astQuery + ";");
        api.sqlQuery(astQuery.toString());
        return new Database(this.api, name, engine);
    }

    // public Database create(String name, Handler engine, Map<String, String>
    // connectionArgs) {
    // return this.create(name, engine.getName(), connectionArgs);
    // }

    // public Database create(String name, String engine) {
    // return this.create(name, engine, null);
    // }

    // public Database create(String name, Handler engine) {
    // return this.create(name, engine.getName());
    // }

    public void drop(String name) {
        String astQuery = String.format("DROP DATABASE %s", name);
        api.sqlQuery(astQuery);
    }

}
