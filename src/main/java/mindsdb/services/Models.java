package mindsdb.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Model;
import mindsdb.models.ModelVersion;
import mindsdb.models.Project;
import tech.tablesaw.api.Table;

public class Models {
    private final Project project;
    private final RestAPI api;

    public Models(Project project, RestAPI api) {
        this.project = project;
        this.api = api;
    }

    public List<Model> listModels(String name) {
        Map<String, Object> filters = new HashMap<>();
        if (name != null) {
            filters.put("name", name);
        }
        filters.put("active", 1);

        StringBuilder astQuery = new StringBuilder("SELECT * FROM MODELS");
        if (!filters.isEmpty()) {
            astQuery.append(" WHERE ");
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                astQuery.append(entry.getKey()).append(" = ").append("\'").append(entry.getValue()).append("\'")
                        .append(" AND ");
            }
            astQuery.delete(astQuery.length() - 5, astQuery.length());
        }

        astQuery.append(";");

        Table response = project.query(astQuery.toString()).fetch();

        return response.stream().map(row -> {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < response.columnCount(); i++) {
                data.put(response.columnNames().get(i), row.getString(i));
            }
            return new Model(project, data);
        }).toList();
    }

    public List<ModelVersion> listModelsWithVersion(String name, Integer version) {
        Map<String, Object> filters = new HashMap<>();
        if (name != null) {
            filters.put("name", name);
        }
        if (version != null) {
            filters.put("version", version);
        }

        StringBuilder astQuery = new StringBuilder("SELECT * FROM MODELS");
        if (!filters.isEmpty()) {
            astQuery.append(" WHERE ");
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                astQuery.append(entry.getKey()).append(" = ").append("\'").append(entry.getValue()).append("\'")
                        .append(" AND ");
            }
            astQuery.delete(astQuery.length() - 5, astQuery.length());
        }

        astQuery.append(";");

        Table response = project.query(astQuery.toString()).fetch();

        return response.stream().map(row -> {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < response.columnCount(); i++) {
                data.put(response.columnNames().get(i), row.getObject(i));
            }
            return new ModelVersion(project, data);
        }).toList();
    }

    public Model getModel(String name) {
        List<Model> models = listModels(name);
        if (models.isEmpty()) {
            throw new IllegalArgumentException("Model not found");
        }
        return models.get(0);
    }

    public ModelVersion getModelWithVersion(String name, Integer version) {
        List<ModelVersion> models = listModelsWithVersion(name, version);
        if (models.isEmpty()) {
            throw new IllegalArgumentException("Model version not found");
        }
        return models.get(0);
    }

    public Model create(
            String name,
            String predict,
            String engine,
            String query,
            String database,
            Map<String, String> options,
            Map<String, Object> timeseriesOptions) {

        StringBuilder astQuery = new StringBuilder();

        // Start building the query
        String modelName = this.project.getName() + "." + name;
        astQuery.append("CREATE MODEL ").append(modelName);

        if (query != null) {
            astQuery.append(" FROM ").append(database).append(" (").append(query).append(")");
        }

        if (predict != null) {
            astQuery.append(" PREDICT ").append(predict);
        }

        if (timeseriesOptions != null) {
            if (timeseriesOptions.containsKey("order")) {
                astQuery.append(" ORDER BY ").append(timeseriesOptions.get("order"));
            }
            if (timeseriesOptions.containsKey("group")) {
                Object group = timeseriesOptions.get("group");
                if (group instanceof String) {
                    astQuery.append(" GROUP BY ").append(group);
                } else if (group instanceof List) {
                    astQuery.append(" GROUP BY ").append(String.join(", ", (List<String>) group));
                }
            }
            if (timeseriesOptions.containsKey("window")) {
                astQuery.append(" WINDOW ").append(timeseriesOptions.get("window"));
            }
            if (timeseriesOptions.containsKey("horizon")) {
                astQuery.append(" HORIZON ").append(timeseriesOptions.get("horizon"));
            }
        }

        if (options != null && !options.isEmpty()) {
            astQuery.append(" USING ");
            options.forEach((key, value) -> astQuery.append(key).append(" = '").append(value).append("', "));
            // Remove the trailing comma and space
            astQuery.setLength(astQuery.length() - 2);
        }

        astQuery.append(";");

        Table modelData = project.api.sqlQuery(astQuery.toString());

        Map<String, Object> dataMap = new HashMap<>();
        for (String columnName : modelData.columnNames()) {
            dataMap.put(columnName, modelData.column(columnName).get(0));
        }

        // Return new instance
        return new Model(project, dataMap);
    }

}