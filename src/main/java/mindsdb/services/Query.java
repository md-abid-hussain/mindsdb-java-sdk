package mindsdb.services;

import kong.unirest.core.UnirestException;
import lombok.Getter;
import lombok.Setter;
import mindsdb.connectors.RestAPI;
import tech.tablesaw.api.Table;

/**
 * Query class for handling SQL queries.
 */
@Getter
@Setter
public class Query {
    private final RestAPI api;
    private String sql;
    private final String database;

    /**
     * Constructs a new Query instance with the provided API, SQL query, and database.
     * @param api    - RestAPI object
     * @param sql   - SQL query
     * @param database  - database name
     */
    public Query(RestAPI api, String sql, String database) {
        this.api = api;
        this.sql = sql;
        this.database = database;
    }

    /**
     * Constructs a new Query instance with the provided API and SQL query.
     * @param api   - RestAPI object
     * @param sql - SQL query
     */
    public Query(RestAPI api, String sql) {
        this(api, sql, null);
    }
    
    /**
     * Fetch the result of the SQL query.
     * @return Table object containing the result of the SQL query
     */
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
