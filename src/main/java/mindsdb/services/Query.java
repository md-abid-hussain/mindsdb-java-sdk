package mindsdb.services;

import kong.unirest.core.UnirestException;
import mindsdb.connectors.RestAPI;
import tech.tablesaw.api.Table;

public class Query {
    private final RestAPI api;
    public String sql;
    public final String database;

    public Query(RestAPI api, String sql, String database) {
        this.api = api;
        this.sql = sql;
        this.database = database;
    }

    public Query(RestAPI api, String sql) {
        this(api, sql, null);
    }

    public Table fetch() {
        try {
            // Sending the SQL query to the API
            return api.sqlQuery(sql, database);
        } catch (UnirestException e) {
            // Handle exception (e.g., logging or rethrowing)
            throw new RuntimeException("Error executing SQL query: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        String processedSql = sql.replace('\n', ' ');
        if (processedSql.length() > 40) {
            processedSql = processedSql.substring(0, 37) + "...";
        }
        return String.format("%s(%s)", this.getClass().getSimpleName(), processedSql);
    }
}
