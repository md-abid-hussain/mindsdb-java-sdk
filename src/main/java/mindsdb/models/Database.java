package mindsdb.models;

import java.util.List;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.services.Tables;

/**
 * 
 * Allows to work with database (datasource): to use tables and make raw queries
 * 
 * To run native query
 * At this moment query is just saved in Qeury object and not executed
 * Query query = database.query("SELECT * FROM table");
 * 
 * This command sends request to server to execute query and return Tablesaw
 * Table
 * Table table = query.fetch();
 * Has list of tables in .tables attribute.
 */
public class Database {
    public String name;
    public String engine;
    public final RestAPI api;
    public final Tables tables;
    public Project project;

    public Database(Project project, String name, String engine) {
        this.name = name;
        this.engine = engine;
        this.api = project.api;
        this.tables = new Tables(this, api);
        this.project = project;
    }

    public Database(RestAPI api, String name, String engine) {
        this.name = name;
        this.engine = engine;
        this.api = api;
        this.tables = new Tables(this, api);

    }

    /**
     * Create a new Query object
     * 
     * @param sql - SQL query string
     * @return Query object
     */
    public Query query(String sql) {
        return new Query(this.api, sql, this.name);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), this.name);
    }

    /**
     * List all tables in database
     * 
     * @return List of Table objects
     */
    public List<Table> listTables() {
        return tables.list();
    }

    /**
     * Get table by name
     * 
     * @param tableName - name of table
     * @return Table object
     */
    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    /**
     * Drop table by name
     * 
     * @param tableName - name of table
     */
    public void dropTable(String tableName) {
        this.tables.drop(tableName);
    }

    /**
     * Create a table from a query
     * 
     * @param name    - Name of the table
     * @param df      - Table to create the table from
     * @param replace - Replace the table if it already exists
     * @return Table
     */
    public Table createTable(String name, tech.tablesaw.api.Table df, boolean replace) {
        return tables.create(name, df, replace);
    }

    /**
     * Create a table from a query
     * 
     * @param name    - Name of the table
     * @param query   - Query to create the table from
     * @param replace - Replace the table if it already exists
     * @return Table
     */
    public Table createTable(String name, Query query, boolean replace) {
        return tables.create(name, query, replace);
    }

}
