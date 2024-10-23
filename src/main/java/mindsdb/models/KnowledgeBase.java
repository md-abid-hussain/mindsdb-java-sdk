
package mindsdb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.core.UnirestException;
import kong.unirest.core.json.JSONException;
import kong.unirest.core.json.JSONObject;
import lombok.Getter;
import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import tech.tablesaw.api.Row;

/**
 * The KnowledgeBase class represents a knowledge base within a MindsDB project.
 * It extends the Query class and implements the Cloneable interface.
 * 
 * <p>
 * This class provides methods to interact with the knowledge base, including
 * finding data, inserting files, inserting webpages, and inserting data using
 * various formats such as Table objects, Query objects, and maps.
 * </p>
 * 
 * <p>
 * The KnowledgeBase class also supports deep cloning of its instances.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * // Get KnowledgeBase object
 * KnowledgeBase kb = server.knowledgeBases.get("knowledge_base_name");
 * 
 * // Find data in the knowledge base
 * KnowledgeBase result = kb.find("some query", 10);
 * 
 * // Insert files into the knowledge base
 * List<String> filePaths = List.of("/path/to/file1.txt", "/path/to/file2.txt");
 * kb.insertFiles(filePaths);
 * 
 * // Insert webpages into the knowledge base
 * List<String> urls = List.of("http://example.com/page1", "http://example.com/page2");
 * kb.insertWebpages(urls, 2, List.of("filter1", "filter2"));
 * 
 * // Insert data using a Table object
 * tech.tablesaw.api.Table table = tech.tablesaw.api.Table.create("data");
 * kb.insert(table);
 * 
 * // Insert data using a Query object
 * Query query = new Query(api, "SELECT * FROM some_table");
 * kb.insert(query);
 * 
 * // Insert data using a map
 * Map<String, String> dataMap = new HashMap<>();
 * dataMap.put("column1", "value1");
 * dataMap.put("column2", "value2");
 * kb.insert(dataMap);
 * }
 * </pre>
 * 
 * <p>
 * Note: This class requires the Tablesaw library for handling table data and
 * the Unirest library for making HTTP requests.
 * </p>
 * 
 * @see mindsdb.services.Query
 * @see mindsdb.connectors.RestAPI
 * @see tech.tablesaw.api.Table
 * @see kong.unirest.core.UnirestException
 */
@Getter
public class KnowledgeBase extends Query implements Cloneable {
    private RestAPI api;
    private Project project;
    private String name;
    private String tableName;
    private MDBTable storage;
    private Model model;
    private Map<String, Object> params;
    private List<String> metadataColumns;
    private List<String> contentColumns;
    private final String idColumn;
    private String query;
    private Integer limit;

    /**
     * Create a new KnowledgeBase object
     * 
     * @param api     - RestAPI object
     * @param project - Project object
     * @param data    - Map containing knowledge base data
     */
    public KnowledgeBase(RestAPI api, Project project, Map<String, Object> data) {
        super(api, null);
        this.api = api;
        this.project = project;
        this.name = data.containsKey("name") ? data.get("name").toString() : null;
        this.tableName = String.format("%s.%s", project.getName(), name);
        this.storage = null;

        if (data.get("storage") != null) {
            String[] parts = data.get("storage").toString().split("\\.");
            if (parts.length == 2) {
                String databaseName = parts[0];
                String table = parts[1];
                Database db = new Database(project, databaseName, null);
                this.storage = new MDBTable(db, table);
            }
        }

        this.model = null;
        if (data.get("model") != null) {
            this.model = new Model(project, Map.of("name", data.get("model").toString()));
        }

        Object paramObject = data.getOrDefault("params", new HashMap<>());
        if (paramObject instanceof String string) {
            try {
                paramObject = new JSONObject(string).toMap();
            } catch (JSONException e) {
                paramObject = new HashMap<>();
            }
        }

        if (paramObject instanceof Map<?, ?> map) {
            this.params = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Object) {
                    String key = (String) entry.getKey();
                    Object value = entry.getValue();
                    this.params.put(key, value);
                }
            }
        } else {
            this.params = new HashMap<>();
        }

        Object metadataColumnsObj = this.params.getOrDefault("metadata_columns", new ArrayList<>());
        if (metadataColumnsObj instanceof List<?> list) {
            this.metadataColumns = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String string) {
                    this.metadataColumns.add(string);
                }
            }
        } else {
            this.metadataColumns = new ArrayList<>();
        }

        this.contentColumns = new ArrayList<>();
        Object contentColumnsObj = this.params.get("content_columns");
        if (contentColumnsObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String string) {
                    this.contentColumns.add(string);
                }
            }
        }

        this.idColumn = (String) this.params.getOrDefault("id_column", null);
        this.query = null;
        this.limit = null;
        updateQuery();
    }

    @Override
    public String toString() {
        return String.format("%s(%s.%s)", this.getClass().getSimpleName(), this.project.getName(), this.name);
    }

    /**
     * Find data in the knowledge base
     * 
     * @param query - query string
     * @param limit - limit the number of results
     * @return KnowledgeBase object
     */
    public KnowledgeBase find(String query, Integer limit) {
        KnowledgeBase kb;
        try {
            kb = this.clone();
            kb.query = query;
            kb.limit = limit;
            kb.updateQuery();

            return kb;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    /**
     * Find data in the knowledge base
     * 
     * @param query - query string
     * @return KnowledgeBase object
     */
    public KnowledgeBase find(String query) {
        return find(query, 100);
    }

    private void updateQuery() {
        StringBuilder astQuery = new StringBuilder("SELECT * FROM ").append(this.tableName);
        if (this.query != null) {
            astQuery.append(" WHERE CONTENT = '").append(this.query).append("'");
        }

        if (this.limit != null) {
            astQuery.append(" LIMIT ").append(this.limit);
        }

        astQuery.append(";");
        this.setSql(astQuery.toString());
    }

    /**
     * Insert files into the knowledge base
     * 
     * @param filePaths - list of file paths to insert
     */
    public void insertFiles(List<String> filePaths) {
        try {
            this.api.insertFilesIntoKnowledgeBase(this.project.getName(), this.name, filePaths);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to insert files into knowledge base", e);
        }
    }

    /**
     * Insert webpages into the knowledge base
     * 
     * @param urls       - list of URLs to insert
     * @param crawlDepth - depth of the crawl
     * @param filters    - list of filters
     */
    public void insertWebpages(List<String> urls, int crawlDepth, List<String> filters) {
        try {
            this.api.insertWebpagesIntoKnowledgeBase(this.project.getName(), this.name, urls, crawlDepth, filters);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to insert webpages into knowledge base", e);
        }
    }

    /**
     * Insert data into the knowledge base using a Table object
     * 
     * @param data - Table object containing the data to insert
     */
    public void insert(tech.tablesaw.api.Table data) {
        StringBuilder valueString = new StringBuilder();
        for (Row row : data) {
            valueString.append("(");
            for (int i = 0; i < row.columnCount(); i++) {
                Object cellValue = row.getObject(i);
                if (cellValue instanceof String rowData) {
                    // Escape single quotes
                    rowData = rowData.replace("'", "\\\\'");
                    // Escape double quotes
                    rowData = rowData.replace("\"", "\\\"");
                    valueString.append("'").append(rowData).append("'");
                } else {
                    valueString.append(cellValue);
                }
                if (i < row.columnCount() - 1) {
                    valueString.append(", ");
                }
            }
            valueString.append("), ");
        }

        // Remove the trailing comma and space
        valueString.delete(valueString.length() - 2, valueString.length());

        StringBuilder columnNames = new StringBuilder();
        for (String column : data.columnNames()) {
            columnNames.append(column);
            columnNames.append(", ");
        }

        // Remove the trailing comma and space
        columnNames.delete(columnNames.length() - 2, columnNames.length());

        String sqlQuery = String.format("INSERT INTO %s (%s) VALUES %s;", this.tableName, columnNames.toString(),
                valueString.toString());

        this.api.sqlQuery(sqlQuery, this.project.getName());
    }

    /**
     * Insert data into the knowledge base using a Query object
     * 
     * @param data Query object containing the data to insert
     */
    public void insert(Query data) {
        String astQuery;
        if (data.getDatabase() != null) {
            astQuery = String.format("INSERT INTO %s SELECT * FROM %s (%s) ;", this.tableName, data.getDatabase(),
                    data.getSql());
        } else {
            astQuery = String.format("INSERT INTO %s (%s)", this.tableName, data.getSql());
        }

        this.api.sqlQuery(astQuery, data.getDatabase());
    }

    /**
     * Insert data into the knowledge base using a map
     * 
     * @param data Map containing the data to insert
     */
    public void insert(Map<String, String> data) {
        tech.tablesaw.api.Table table = tech.tablesaw.api.Table.create("data");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            table.addColumns(tech.tablesaw.api.StringColumn.create(entry.getKey(), entry.getValue()));
        }

        insert(table);
    }

    @Override
    protected KnowledgeBase clone() throws CloneNotSupportedException {
        try {
            KnowledgeBase cloned = (KnowledgeBase) super.clone();

            // Deep copy mutable fields
            cloned.params = new HashMap<>(this.params);
            cloned.metadataColumns = new ArrayList<>(this.metadataColumns);
            cloned.contentColumns = new ArrayList<>(this.contentColumns);

            // Handle deep copy for storage if necessary
            if (this.storage != null) {
                cloned.storage = new MDBTable(this.storage.db, this.storage.name);
            }

            // Handle deep copy for model if necessary
            if (this.model != null) {
                cloned.model = new Model(this.project, Map.of("name", this.model.getName()));
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}