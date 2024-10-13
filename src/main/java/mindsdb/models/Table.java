package mindsdb.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.utils.DataFrame;

public class Table extends Query {
    private String name;
    private String tableName;
    private Database database;
    private Map<String, String> filters;
    private Integer limit;
    private String trackColumn;
    private String sql;

    public Table(Database database, String name) {
        super(database.api, "");
        this.name = name;
        this.database = database;
        this.tableName = database.name + "." + name;
        this.filters = new HashMap<>();
        this.limit = null;
        this.trackColumn = null;
    }

    public Table(RestAPI api, String sql) {
        super(api, sql);
        ;
    }

    private String filterRepr() {
        if (filters.isEmpty()) {
            return "";
        }
        String filtersStr = filters.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(", "));
        return ", " + filtersStr;
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
        this.sql = queryBuilder.toString();
    }

    // Creates a copy of the filters to ensure immutability of the original table
    private Map<String, String> copyFilters() {
        return new HashMap<>(this.filters);
    }

    /**
     * Filter the table by key-value pairs
     * >>> table.filter("a=1", "b=2")
     * 
     * @param filters
     * @return Table object with the filters set
     */
    public Table filter(String... filters) {
        for (String param : filters) {
            if (param.split("=").length != 2) {
                throw new IllegalArgumentException("Filter parameters should be in key=value format");
            }
        }

        // Create a new Table object (deep copy) and copy the filters
        Table queryTable = new Table(this.database, this.name);
        queryTable.filters = this.copyFilters(); // Copy existing filters

        for (String param : filters) {
            String[] keyValue = param.split("=");
            queryTable.filters.put(keyValue[0], keyValue[1]);
        }
        queryTable.updateQuery();
        return queryTable;
    }

    /**
     * Limit the number of rows returned by the query
     * 
     * @param limit
     * @return Table object with the limit set
     */
    public Table limit(Integer limit) {
        Table queryTable = new Table(this.database, this.name);
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
    public Table track(String column) {
        Table queryTable = new Table(this.database, this.name);
        queryTable.filters = this.copyFilters();
        queryTable.limit = this.limit;
        queryTable.trackColumn = column;
        queryTable.updateQuery();
        return queryTable;
    }

    /**
     * 
     * @param query
     * @return
     */

    public Query insert(Table query) {
        if (query.database != null) {
            this.sql = String.format("INSERT INTO %s (%s)", this.tableName, query.sql);
        } else {
            this.sql = String.format("INSERT INTO %s (%s)", this.tableName, query.name);
        }

        // if (ContextManager.isSaving()) {
        // return new Query(null, sql, null);
        // }

        this.database.api.sqlQuery(this.sql);
        return null;
    }

    public Query insert(DataFrame query) {
        List<Map<String, Object>> rows = query.getRows();
        List<String> columns = query.getColumnNames();

        StringBuilder astQuery = new StringBuilder();
        astQuery.append("INSERT INTO ");
        astQuery.append(this.tableName);
        astQuery.append(" (");
        astQuery.append(String.join(", ", columns));
        astQuery.append(") VALUES ");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            astQuery.append("(");
            for (int j = 0; j < columns.size(); j++) {
                Object value = row.get(columns.get(j));
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
            if (i < rows.size() - 1) {
                astQuery.append(", ");
            }
        }

        this.sql = astQuery.toString();

        // if (ContextManager.isSaving()) {
        // return new Query(null, sql, null);
        // }

        this.database.api.sqlQuery(this.sql);
        return null;

    }

    // public Query insert(Object query) {
    // if (query instanceof Table) {
    // Table queryObj = (Table) query;
    // if (queryObj.database != null) {
    // this.sql = String.format("INSERT INTO %s (%s)", this.tableName,
    // queryObj.sql);
    // } else {
    // this.sql = String.format("INSERT INTO %s (%s)", this.tableName,
    // queryObj.name);
    // }
    // } else if (query instanceof DataFrame) {
    // List<Map<String, Object>> rows = ((DataFrame) query).getRows();
    // List<String> columns = ((DataFrame) query).getColumnNames();

    // StringBuilder astQuery = new StringBuilder();
    // astQuery.append("INSERT INTO ");
    // astQuery.append(this.tableName);
    // astQuery.append(" (");
    // astQuery.append(String.join(", ", columns));
    // astQuery.append(") VALUES ");

    // for (int i = 0; i < rows.size(); i++) {
    // Map<String, Object> row = rows.get(i);
    // astQuery.append("(");
    // for (int j = 0; j < columns.size(); j++) {
    // Object value = row.get(columns.get(j));
    // if (value instanceof String) {
    // astQuery.append("'");
    // astQuery.append(value);
    // astQuery.append("'");
    // } else {
    // astQuery.append(value);
    // }
    // if (j < columns.size() - 1) {
    // astQuery.append(", ");
    // }
    // }
    // astQuery.append(")");
    // if (i < rows.size() - 1) {
    // astQuery.append(", ");
    // }
    // }

    // this.sql = astQuery.toString();

    // } else {
    // if (query == null) {
    // throw new IllegalArgumentException("Query object cannot be null.");
    // }
    // throw new IllegalArgumentException("Invalid query type: " +
    // query.getClass().getName());
    // }

    // if (ContextManager.isSaving()) {
    // return new Query(null, sql, null);
    // }

    // this.database.api.sqlQuery(this.sql);
    // return null;
    // }

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

        this.sql = deleteQuery.toString();

        // if (ContextManager.isSaving()) {
        // return new Query(null, sql, null);
        // }

        this.database.api.sqlQuery(this.sql);
        // return null;
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

        this.sql = updateQuery.toString();

        // if (ContextManager.isSaving()) {
        // return new Query(null, sql, null);
        // }

        this.database.api.sqlQuery(this.sql);
        // return null;
    }

    /**
     * Update table from subquery
     * 
     * @param subQuery  a Query object representing the subquery
     * @param onColumns list of columns to map subselect to table ['a', 'b', ...]
     * @return a Query object representing the update operation
     */
    public void update(Query subQuery, List<String> onColumns) {
        if (subQuery == null) {
            throw new IllegalArgumentException("Subquery cannot be null.");
        }
        if (onColumns == null || onColumns.isEmpty()) {
            throw new IllegalArgumentException("On columns list cannot be null or empty.");
        }

        StringBuilder updateQuery = new StringBuilder("UPDATE ");
        updateQuery.append(this.tableName);
        updateQuery.append(" ON ");
        updateQuery.append(String.join(", ", onColumns));
        updateQuery.append(" FROM (");
        updateQuery.append(subQuery.sql);
        updateQuery.append(")");

        this.sql = updateQuery.toString();

        // if (ContextManager.isSaving()) {
        // return new Query(null, sql, null);
        // }

        this.database.api.sqlQuery(this.sql);
        // return null;
    }

}
