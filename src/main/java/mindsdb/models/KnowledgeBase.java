package mindsdb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.core.UnirestException;
import kong.unirest.core.json.JSONException;
import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import tech.tablesaw.api.Row;

public class KnowledgeBase extends Query implements Cloneable {
    public RestAPI api;
    public Project project;
    public String name;
    public String tableName;
    public Table storage;
    public Model model;
    public Map<String, Object> params;
    public List<String> metadataColumns;
    public List<String> contentColumns;
    public String idColumn;
    public String query;
    public Integer limit;
    public String sql;

    public KnowledgeBase(RestAPI api, Project project, Map<String, Object> data) {
        super(api, null);
        this.api = api;
        this.project = project;
        this.name = data.get("name").toString();
        this.tableName = String.format("%s.%s", project.getName(), name);
        this.storage = null;

        if (data.get("storage") != null) {
            String[] parts = data.get("storage").toString().split("\\.");
            if (parts.length == 2) {
                String databaseName = parts[0];
                String table = parts[1];
                Database db = new Database(project, databaseName, null);
                this.storage = new Table(db, table);
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

        super.sql = this.sql;
    }

    @Override
    public String toString() {
        return String.format("%s(%s.%s)", this.getClass().getSimpleName(), this.project.getName(), this.name);
    }

    public KnowledgeBase find(String query, Integer limit) {
        KnowledgeBase kb = this.clone();
        kb.query = query;
        kb.limit = limit;
        kb.updateQuery();
        return kb;
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
        this.sql = astQuery.toString();
    }

    public void insertFiles(List<String> filePaths) {
        try {
            this.api.insertFilesIntoKnowledgeBase(this.project.getName(), this.name, filePaths);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to insert files into knowledge base", e);
        }
    }

    public void insertWebpages(List<String> urls, int crawlDepth, List<String> filters) {
        try {
            this.api.insertWebpagesIntoKnowledgeBase(this.project.getName(), this.name, urls, crawlDepth, filters);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to insert webpages into knowledge base", e);
        }
    }

    public void insert(tech.tablesaw.api.Table data) {
        StringBuilder valueString = new StringBuilder();
        for (Row row : data) {
            valueString.append("(");
            for (int i = 0; i < row.columnCount(); i++) {
                valueString.append(row.getObject(i));
                if (i < row.columnCount() - 1) {
                    valueString.append(", ");
                }
            }
            valueString.append("), ");
        }

        StringBuilder columnNames = new StringBuilder();
        for (String column : data.columnNames()) {
            columnNames.append(column);
            columnNames.append(", ");
        }

        String astQuery = String.format("INSERT INTO %s (%s) VALUES %s;", this.tableName, columnNames.toString(),
                valueString.toString());

        this.api.sqlQuery(astQuery, this.database);
    }

    public void insert(Query data) {

        String astQuery;
        if (data.database != null) {
            astQuery = String.format("INSERT INTO %s SELECT * FROM %s (%s) ;", this.tableName, data.database, data.sql);
        } else {
            astQuery = String.format("INSERT INTO %s (%s)", this.tableName, data.sql);
        }

        this.api.sqlQuery(astQuery, this.database);

    }

    public void insert(Map<String, String> data) {
        tech.tablesaw.api.Table table = tech.tablesaw.api.Table.create("data");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            table.addColumns(tech.tablesaw.api.StringColumn.create(entry.getKey(), entry.getValue()));
        }

        insert(table);
    }

    @Override
    protected KnowledgeBase clone() {
        try {
            KnowledgeBase cloned = (KnowledgeBase) super.clone();

            // Deep copy mutable fields
            cloned.params = new HashMap<>(this.params);
            cloned.metadataColumns = new ArrayList<>(this.metadataColumns);
            cloned.contentColumns = new ArrayList<>(this.contentColumns);

            // Handle deep copy for storage if necessary
            if (this.storage != null) {
                cloned.storage = new Table(this.storage.db, this.storage.name);
            }

            // Handle deep copy for model if necessary
            if (this.model != null) {
                cloned.model = new Model(this.project, Map.of("name", this.model.name));
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}