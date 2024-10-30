package mindsdb.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.services.Tables;

/**
 * The Database class represents a database in the MindsDB system.
 * It provides methods to interact with the database, such as querying,
 * listing tables, getting a specific table, dropping a table, and creating
 * tables.
 * 
 * <p>
 * The Database class can be instantiated using different constructors depending
 * on
 * the available information (e.g., with a Project object or a RestAPI object).
 * </p>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code
 * Server server = MindsDB.connect();
 * Database db = server.getDatabase("my_database");
 * Query query = db.query("SELECT * FROM my_table");
 * List<MDBTable> tables = db.listTables();
 * MDBTable table = db.getTable("my_table");
 * db.dropTable("my_table");
 * MDBTable newTable = db.createTable("new_table", someDataFrame, true);
 * }
 * </pre>
 * 
 * The class also overrides the {@code toString} method to provide a string
 * representation
 * of the Database object.
 * 
 * @see Project
 * @see RestAPI
 * @see Tables
 * @see Query
 * @see MDBTable
 */
@Getter
@Setter
public class Database {
    private String name;
    private String engine;
    private RestAPI api;
    /**
     * Tables object
     */
    public Tables tables;
    private Project project;

    /**
     * Create a new Database object
     * 
     * @param project - Project object
     * @param name    - Name of the database
     * @param engine  - Engine of the database
     */
    public Database(Project project, String name, String engine) {
        this.name = name;
        this.engine = engine;
        this.api = project.getApi();
        this.tables = new Tables(this, api);
        this.project = project;
    }

    /**
     * Create a new Database object
     * 
     * @param project - Project object
     * @param name    - Name of the database
     */
    public Database(Project project, String name) {
        this(project, name, null);
    }

    /**
     * Create a new Database object
     * 
     * @param api    - RestAPI object
     * @param name   - Name of the database
     * @param engine - Engine of the database
     */
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
        System.out.println("Querying database: " + this.name);
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
    public List<MDBTable> listTables() {
        return tables.list();
    }

    /**
     * Get table by name
     * 
     * @param tableName - name of table
     * @return Table object
     */
    public MDBTable getTable(String tableName) {
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
    public MDBTable createTable(String name, tech.tablesaw.api.Table df, boolean replace) {
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
    public MDBTable createTable(String name, Query query, boolean replace) {
        return tables.create(name, query, replace);
    }

}
