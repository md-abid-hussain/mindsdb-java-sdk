package mindsdb.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Model {

    public Project project;
    public Map<String, Object> data;
    public String name;
    public Integer version;

    public Model(Project project, Map<String, Object> data) {
        this.project = project;
        this.name = data.get("name").toString();
        this.version = data.get("version") != null ? Integer.valueOf(data.get("version").toString()) : null;
        this.data = data;
    }

    private String getIdentifier() {
        String identifier = project.getName() + "." + name;

        if (version != null) {
            identifier += "." + version;
        }

        return identifier;
    }

    @Override
    public String toString() {

        String versionStr = (version != null) ? ", version=" + version : data.get("version").toString();
        return String.format("%s(%s%s, status=%s)", getClass().getSimpleName(), name, versionStr,
                data.get("status"));
    }

    /**
     * Make prediction with the model
     * 
     * @param data   input data
     * @param params prediction parameters
     * @return prediction result in Tablesaw Table
     */
    public tech.tablesaw.api.Table predict(tech.tablesaw.api.Table data, Map<String, String> params) {
        return project.api.modelPredict(project.getName(), name, data, params, version);
    }

    /**
     * Make prediction with the model
     * 
     * @param data input data
     * @return prediction result in Tablesaw Table
     */
    public tech.tablesaw.api.Table predict(tech.tablesaw.api.Table data) {
        return project.api.modelPredict(project.getName(), name, data, null,
                version);
    }

    /**
     * Make prediction with the model
     * 
     * @param data   input data
     * @param params
     * @return prediction result in Tablesaw Table
     */
    public tech.tablesaw.api.Table predict(Map<String, String> data, Map<String, String> params) {
        return project.api.modelPredict(project.getName(), name, data, params, version);
    }

    /**
     * Make prediction with the model
     * 
     * @param data input data
     * @return prediction result in Tablesaw Table
     */
    public tech.tablesaw.api.Table predict(Map<String, String> data) {
        return project.api.modelPredict(project.getName(), name, data, null,
                version);
    }

    public void waitComplete() throws InterruptedException {
        for (int i = 0; i < 400; i++) {
            TimeUnit.MILLISECONDS.sleep(300);
            String status = getStatus();
            if (null == status) {
                break;
            } else {
                switch (status) {
                    case "generating", "training" -> {
                        continue;
                    }
                    case "error" -> throw new RuntimeException("Training failed: " + data.get("error"));
                    default -> {
                    }
                }
            }
        }
    }

    /**
     * Get status of the model
     * 
     * @return status of the model
     */
    public String getStatus() {
        refresh();
        return (String) data.get("status");
    }

    private Map<String, Object> refresh() {
        Model model = version != null ? project.getModelWithVersion(name, version) : project.getModel(name);
        this.data = model.data;
        return this.data;
    }

    /**
     * Return description of the model
     * 
     * @param type describe type (for lightwood is models, ensemble, features),
     *             optional
     * @return dataframe with result of description
     */
    public tech.tablesaw.api.Table describe(String type) {
        String identifier = String.format("%s.%s", project.getName(), name);
        if (type != null) {
            identifier += "." + type;
        }
        String astQuery = String.format("DESCRIBE %s;", identifier);

        return project.api.sqlQuery(astQuery);
    }

    public tech.tablesaw.api.Table describe() {
        return describe(null);
    }

    /**
     * List all versions of the model
     * 
     * @return list of ModelVersion objects
     */
    public List<ModelVersion> listVersions() {
        return project.listModelsWithVersion(this.name, null);
    }

    /**
     * Get version of the model
     * 
     * @param num version number
     * @return ModelVersion object
     */
    public ModelVersion getVersion(Integer num) {
        return project.listModelsWithVersion(name, num).stream()
                .filter(m -> Integer.valueOf(m.version.toString()).equals(num))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
    }

    /**
     * Drop version of the model
     * 
     * @param num version number
     */
    public void dropVersion(int num) {
        project.dropModelVersion(name, num);
    }

    /**
     * Set active version of the model
     * 
     * @param version version number
     */
    public void setActive(int version) {
        String sql = String.format("SET active = %s.%d", name, version);
        project.api.sqlQuery(sql);
        refresh();
    }

    /**
     * Call retrain of the model
     *
     * @param query    SQL string to get data for retraining, optional
     * @param database Database to get data for retraining, optional
     * @param options  Parameters for retraining model, optional
     * @param engine   ML engine, optional
     * @return Model object
     */
    public Model retrain(String query, String database, Map<String, String> options, String engine) {
        return _retrain("RETRAIN", query, database, options, engine);
    }

    /**
     * Call finetune of the model
     *
     * @param query    SQL string to get data for fine-tuning, optional
     * @param database Database to get data for fine-tuning, optional
     * @param options  Parameters for fine-tuning model, optional
     * @param engine   ML engine, optional
     * @return Model object
     */
    public Model finetune(String query, String database, Map<String, String> options, String engine) {
        return _retrain("FINETUNE", query, database, options, engine);
    }

    private Model _retrain(String operation, String query, String database, Map<String, String> options,
            String engine) {
        if (options == null) {
            options = new HashMap<>();
        }
        if (engine != null) {
            options.put("engine", engine);
        }

        String sql = createAstQuery(operation, query, database, options);
        tech.tablesaw.api.Table modelData = project.api.sqlQuery(sql);

        Map<String, Object> dataMap = new HashMap<>();
        for (String columnName : modelData.columnNames()) {
            dataMap.put(columnName, modelData.column(columnName).get(0));
        }

        // Return new instance
        return new Model(project, dataMap);
    }

    private String createAstQuery(String operation, String query, String database, Map<String, String> options) {
        String identifier = getIdentifier();
        StringBuilder sqlBuilder = new StringBuilder();

        String databaseName = database != null ? database : project.getName();

        sqlBuilder.append(operation)
                .append(" MODEL ")
                .append(identifier);

        if (query != null) {
            sqlBuilder.append(" FROM ").append(databaseName).append(" (").append(query).append(")");
        }

        if (!options.isEmpty()) {
            sqlBuilder.append(" USING ");
            options.forEach((key, value) -> sqlBuilder.append(key).append(" = '").append(value).append("', "));
            sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove trailing comma and space
        }

        return sqlBuilder.toString();
    }

}