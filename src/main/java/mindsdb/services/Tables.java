package mindsdb.services;

import java.util.List;

import lombok.Getter;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.MDBTable;
import tech.tablesaw.api.Table;

@Getter
public class Tables {
    public final Database database;
    public final RestAPI api;

    public Tables(Database database, RestAPI api) {
        this.database = database;
        this.api = api;
    }

    private List<String> listTables() {
        Table response = database.query("SHOW TABLES").fetch();
        String firstColumnName = response.columnNames().get(0);

        return response.column(firstColumnName).asList().stream()
                .map(Object::toString)
                .toList();

    }

    /**
     * List all tables in the database
     * 
     * @return List of Table
     */
    public List<MDBTable> list() {
        return listTables().stream()
                .map(tableName -> new MDBTable(this.database, tableName))
                .toList();
    }

    /**
     * Get a table by name
     * 
     * @param tableName
     * @return Table
     */
    public MDBTable get(String tableName) {
        return new MDBTable(this.database, tableName);
    }

    /**
     * Create a table from a query
     * 
     * @param name    Name of the table
     * @param query   Query to create the table from
     * @param replace Replace the table if it already exists
     * @return Table
     */
    public MDBTable create(String name, Query query, boolean replace) {
        String tableName = this.database.getName() + "." + name;
        String astQuery;
        String replaceStr = "";
        if (replace) {
            replaceStr = " OR REPLACE";
        }
        if (query.getDatabase() != null) {

            astQuery = String.format("CREATE%s TABLE %s SELECT * FROM %s (%s)", replaceStr, tableName,
                    query.getDatabase(), query.getSql());
        } else {
            astQuery = String.format("CREATE%s TABLE %s (%s)", replaceStr, tableName, query.getSql());
        }

        this.api.sqlQuery(astQuery);
        return new MDBTable(this.database, name);
    }

    /**
     * Create a table from a DataFrame
     * 
     * @param name    Name of the table
     * @param query   DataFrame to create the table from
     * @param replace Replace the table if it already exists
     * @return Table
     */
    public MDBTable create(String name, tech.tablesaw.api.Table df, Boolean replace) {
        this.api.uploadFile(name, df);

        if (this.database.getName().equals("files")) {
            if (name.contains(".")) {
                name = name.split("\\.")[0];
            }

            return new MDBTable(this.database, name);
        }

        throw new IllegalArgumentException("Only files database is supported for now");
    }

    /**
     * Drop a table by name
     * 
     * @param tableName
     */
    public void drop(String tableName) {
        String sql = String.format("DROP TABLE %s", this.database.getName() + "." + tableName);
        this.api.sqlQuery(sql);
    }

}
