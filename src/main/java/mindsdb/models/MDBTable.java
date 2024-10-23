package mindsdb.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import mindsdb.services.Query;
import tech.tablesaw.api.Row;

/**
 * The MDBTable class represents a table in the MindsDB system.
 * It extends the Query class and provides methods to interact with the table,
 * such as filtering, limiting, tracking, inserting, deleting, and updating
 * data.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code
 * Database db = server.getDatabase("my_database");
 * MDBTable table = db.getTable("my_table");
 * table.filter("column1=value1").limit(10).track("column2");
 * table.insert(someQuery);
 * table.insert(someDataFrame);
 * table.delete("column1=value1");
 * table.update(Map.of("column1", "new_value"), "column2=value2");
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * The class also overrides the {@code toString} method to provide a string
 * representation of the MDBTable object.
 * </p>
 * 
 * @see Database
 * @see Query
 * @see tech.tablesaw.api.Table
 */
@Getter
public class MDBTable extends Query {
    public String name;
    public String tableName;
    public Database db;
    private Map<String, String> filters;
    private Integer limit;
    private String trackColumn;

    /**
     * Create a new Mindsdb Table object
     * 
     * @param database - Database object
     * @param name     - Name of the table
     */
    public MDBTable(Database database, String name) {
        super(database.getApi(), "");
        this.name = name;
        this.db = database;
        this.tableName = database.getName() + "." + name;
        this.filters = new HashMap<>();
        this.limit = null;
        this.trackColumn = null;
        this.updateQuery();
    }

    /**
     * Create a new Mindsdb Table object
     * 
     * @param project - Project object
     * @param name    - Name of the table
     */
    public MDBTable(Project project, String name) {
        super(project.getApi(), "");
        this.name = name;
        this.tableName = project.getName() + "." + name;
        this.updateQuery();

    }

    private String filterRepr() {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        String filtersStr = filters.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(", "));
        return ", " + filtersStr;
    }

    private void updateQuery() {
        // Initialize the query with SELECT * FROM table_name
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(this.tableName);

        // If there are filters, add a WHERE clause
        if (filters != null && !filters.isEmpty()) {
            queryBuilder.append(" WHERE ");
            String whereClause = filters.entrySet().stream()
                    .map(e -> e.getKey() + " = '" + e.getValue() + "'")
                    .collect(Collectors.joining(" AND "));

            if (this.trackColumn != null) {
                whereClause += "AND " + this.trackColumn + " > last";
            }
            queryBuilder.append(whereClause);
        }

        // If a limit is set, append the LIMIT clause
        if (this.limit != null) {
            queryBuilder.append(" LIMIT ");
            queryBuilder.append(this.limit);
        }

        // Convert the StringBuilder to a string and assign it to the SQL query
        this.setSql(queryBuilder.toString());
    }

    // Creates a copy of the filters to ensure immutability of the original table
    private Map<String, String> copyFilters() {
        return new HashMap<>(this.filters);
    }

    @Override
    public String toString() {
        String limitStr = "";
        if (this.limit != null) {
            limitStr = ", limit=" + this.limit;
        }
        return String.format("%s(%s%s%s)", this.getClass().getSimpleName(), this.tableName, this.filterRepr(),
                limitStr);
    }

    /**
     * Filter the table by key-value pairs
     * >>> table.filter("a=1", "b=2") *
     * 
     * @param filters - Key-value pairs to filter the table by
     * @return Table object with the filters set
     */
    public MDBTable filter(String... filters) {
        for (String param : filters) {
            if (param.split("=").length != 2) {
                throw new IllegalArgumentException("Filter parameters should be in key=value format");
            }
        }

        // Create a new Table object (deep copy) and copy the filters
        MDBTable queryTable = new MDBTable(this.db, this.name);
        queryTable.filters = this.copyFilters(); // Copy existing filters

        for (String param : filters) {
            String[] keyValue = param.split("=");
            queryTable.filters.put(keyValue[0], keyValue[1]);
        }
        queryTable.limit = this.limit;
        queryTable.trackColumn = this.trackColumn;
        queryTable.updateQuery();
        return queryTable;
    }

    /**
     * Limit the number of rows returned by the query
     * 
     * @param limit - Number of rows to limit the query to
     * @return Table object with the limit set
     */
    public MDBTable limit(Integer limit) {
        MDBTable queryTable = new MDBTable(this.db, this.name);
        queryTable.filters = this.copyFilters();
        queryTable.limit = limit;
        queryTable.updateQuery();
        return queryTable;
    }

    /**
     * Track the table by a column
     * 
     * @param column
     * @return Table object with the track column set
     */
    public MDBTable track(String column) {
        MDBTable queryTable = new MDBTable(this.db, this.name);
        queryTable.filters = this.copyFilters();
        queryTable.limit = this.limit;
        queryTable.trackColumn = column;
        queryTable.updateQuery();
        return queryTable;
    }

    /**
     * Insert data into table
     * 
     * @param query a Query object representing the data to insert
     */
    public void insert(Query query) {
        String astQuery;
        if (query.getDatabase() != null) {
            String fromQueryString = String.format("SELECT * FROM %s (%s)", query.getDatabase(), query.getSql());
            astQuery = String.format("INSERT INTO %s %s", this.tableName, fromQueryString);
        } else {
            astQuery = String.format("INSERT INTO %s (%s)", this.tableName, query.getSql());
        }
        this.db.getApi().sqlQuery(astQuery);
    }

    /**
     * Insert data into table
     * 
     * @param query a Tablesaw Table object representing the data to insert
     */
    public void insert(tech.tablesaw.api.Table query) {
        List<String> columns = query.columnNames();

        StringBuilder astQuery = new StringBuilder();
        astQuery.append("INSERT INTO ");
        astQuery.append(this.tableName);
        astQuery.append(" (");
        astQuery.append(String.join(", ", columns));
        astQuery.append(") VALUES ");

        for (int index = 0; index < query.rowCount(); index++) {
            Row row = query.row(index);
            astQuery.append("(");

            for (int j = 0; j < columns.size(); j++) {
                Object value = row.getObject(j);
                if (value instanceof String) {
                    astQuery.append("'");
                    astQuery.append(value);
                    astQuery.append("'");
                } else {
                    astQuery.append(value);
                }
                if (j < columns.size() - 1) {
                    astQuery.append(", ");
                }
            }
            astQuery.append(")");
            if (index < query.rowCount() - 1) {
                astQuery.append(", ");
            }
        }

        this.db.getApi().sqlQuery(astQuery.toString());
    }

    /**
     * Deletes record from table using filters
     *
     * >>> table.delete("a=1", "b=2")
     *
     * @param filters
     * @return a Query object representing the delete operation
     */
    public void delete(String... filters) {

        StringBuilder deleteQuery = new StringBuilder("DELETE FROM ");
        deleteQuery.append(this.tableName);

        if (filters.length != 0) {
            deleteQuery.append(" WHERE ");
            String whereClause = String.join(" AND ", filters);
            deleteQuery.append(whereClause);
        }

        deleteQuery.append(";");

        this.db.getApi().sqlQuery(deleteQuery.toString());
    }

    /**
     * Update table by condition
     * 
     * @param values  a map representing fields to update
     * @param filters array of filters to filter updated rows, {"column=value", ...}
     * @return a Query object representing the update operation
     */
    public void update(Map<String, Object> values, String... filters) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values map cannot be null or empty.");
        }

        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters cannot be null or empty.");
        }

        StringBuilder updateQuery = new StringBuilder("UPDATE ");
        updateQuery.append(this.tableName);
        updateQuery.append(" SET ");

        String setClause = values.entrySet().stream()
                .map(e -> e.getKey() + " = "
                        + (e.getValue() instanceof String ? "'" + e.getValue() + "'" : e.getValue()))
                .collect(Collectors.joining(", "));
        updateQuery.append(setClause);

        updateQuery.append(" WHERE ");
        String whereClause = String.join(" AND ", filters);
        updateQuery.append(whereClause);

        this.db.getApi().sqlQuery(updateQuery.toString());
    }
}
