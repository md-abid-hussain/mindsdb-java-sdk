package mindsdb.services;

import java.util.List;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Table;
import mindsdb.utils.DataFrame;

public class Tables {
    public final Database database;
    public final RestAPI api;

    public Tables(Database database, RestAPI api) {
        this.database = database;
        this.api = api;
    }

    private List<String> listTables() {
        tech.tablesaw.api.Table response = database.query("SHOW TABLES").fetch();

        // Get the first column name
        // String firstColumnName = response.getColumnNames().get(0);
        String firstColumnName = response.columnNames().get(0);

        // Return the first column as a list of strings
        // return response.getColumn(firstColumnName).stream()
        // .map(Object::toString)
        // .toList();

        return response.column(firstColumnName).asList().stream()
                .map(Object::toString)
                .toList();

    }

    /**
     * List all tables in the database
     * 
     * @return List of Table
     */
    public List<Table> list() {
        return listTables().stream()
                .map(tableName -> new Table(this.database, tableName))
                .toList();
    }

    /**
     * Get a table by name
     * 
     * @param tableName
     * @return Table
     */
    public Table get(String tableName) {
        return new Table(this.database, tableName);
    }

    /**
     * Create a table from a query
     * 
     * @param name    Name of the table
     * @param query   Query to create the table from
     * @param replace Replace the table if it already exists
     * @return Table
     */
    public Table create(String name, Query query, boolean replace) {
        String tableName = this.database.name + "." + name;
        String sql;
        String replaceStr = "";
        if (replace) {
            replaceStr = " OR REPLACE";
        }
        if (query.database != null) {

            sql = String.format("CREATE%s TABLE %s SELECT * FROM %s (%s)'", replaceStr, tableName,
                    query.database, query.sql);
        } else {
            sql = String.format("CREATE%s TABLE %s (%s)", replaceStr, tableName, query.sql);
        }

        // if (ContextManager.isSaving()) {
        // return new Table(this.api, sql);
        // }
        this.api.sqlQuery(sql);
        return new Table(this.database, name);
    }

    /**
     * Create a table from a DataFrame
     * 
     * @param name    Name of the table
     * @param query   DataFrame to create the table from
     * @param replace Replace the table if it already exists
     * @return Table
     */
    public Table create(String name, DataFrame query, boolean replace) {
        // To be implemented
        return null;
    }

    /**
     * Drop a table by name
     * 
     * @param tableName
     */
    public void drop(String tableName) {
        String sql = String.format("DROP TABLE %s", this.database.name + "." + tableName);
        // if (ContextManager.isSaving()) {
        // return new Query(this.api, sql);
        // }
        this.api.sqlQuery(sql);
    }

}
