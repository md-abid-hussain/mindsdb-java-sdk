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
        return response.stream().map(row -> {
            String name = row.getString("NAME");
            String engine = row.getString("ENGINE");
            return new Database(this.api, name, engine);
        }).collect(Collectors.toList());

    }

    /**
     * List all databases
     * 
     * @return List of databases
     */
    public List<Database> list() {
        return listDatabases();
    }

    /**
     * Get database by name
     * 
     * @param name - database name
     * @return Database object
     */
    public Database get(String name) {
        return listDatabases().stream()
                .filter(database -> database.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database doesn't exist"));
    }

    /**
     * Create a new database
     * 
     * @param name           - database name
     * @param engine         - database engine
     * @param connectionArgs - database connection arguments
     * @return Database object
     */
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

    /**
     * Drop database by name
     * 
     * @param name - database name
     */
    public void drop(String name) {
        String astQuery = String.format("DROP DATABASE %s", name);
        api.sqlQuery(astQuery);
    }

}
