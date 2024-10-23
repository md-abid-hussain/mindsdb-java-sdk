package mindsdb.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.KnowledgeBase;
import mindsdb.models.Model;
import mindsdb.models.Project;
import tech.tablesaw.api.Row;
import tech.tablesaw.columns.Column;

/**
 * Service class for managing knowledge bases in a MindsDB project.
 */
public class KnowledgeBases {
    private final Project project;
    private final RestAPI api;

    /**
     * Constructs a new KnowledgeBases instance with the provided project and API.
     * @param project The project associated with the knowledge bases.
     * @param api The API instance used to interact with the backend.
     */
    public KnowledgeBases(Project project, RestAPI api) {
        this.project = project;
        this.api = api;
    }

    private List<KnowledgeBase> listKnowledgeBases(String name) {
        String astQuery = "SELECT * FROM information_schema.KNOWLEDGE_BASES";
        if (name != null) {
            astQuery += " WHERE name = '" + name + "'";
        }
        astQuery += ";";

        tech.tablesaw.api.Table response = api.sqlQuery(astQuery, project.getName());

        List<KnowledgeBase> knowledgeBases = new ArrayList<>();
        for (Row row : response) {
            Map<String, Object> data = new HashMap<>();
            for (Column<?> column : response.columns()) {
                data.put(column.name(), row.getObject(column.name()));
            }
            KnowledgeBase knowledgeBase = new KnowledgeBase(api, project, data);
            knowledgeBases.add(knowledgeBase);
        }

        return knowledgeBases;
    }

    /**
     * List all knowledge bases in the project.
     * @return List of knowledge bases.
     */
    public List<KnowledgeBase> list() {
        return listKnowledgeBases(null);
    }

    /**
     * Get a knowledge base by name.
     * @param name The name of the knowledge base.
     * @return The knowledge base with the specified name.
     * @throws IllegalArgumentException if the knowledge base does not exist.
     */
    public KnowledgeBase get(String name) {
        List<KnowledgeBase> knowledgeBases = listKnowledgeBases(name);
        if (knowledgeBases.isEmpty()) {
            throw new IllegalArgumentException("Knowledge base not found: " + name);
        }
        return knowledgeBases.get(0);
    }

    /**
     * Create a new knowledge base.
     * @param name The name of the knowledge base.
     * @param model The model to use for the knowledge base.
     * @param storage The storage to use for the knowledge base.
     * @param metadataColumns The metadata columns for the knowledge base.
     * @param contentColumns The content columns for the knowledge base.
     * @param idColumn The ID column for the knowledge base.
     * @param params Additional parameters for the knowledge base.
     * @return The created knowledge base.
     */
    public KnowledgeBase create(
            String name,
            Model model,
            mindsdb.models.MDBTable storage,
            List<String> metadataColumns,
            List<String> contentColumns,
            String idColumn,
            Map<String, Object> params) {

        Map<String, Object> paramsOut = new HashMap<>();

        if (metadataColumns != null) {
            paramsOut.put("metadata_columns", metadataColumns);
        }

        if (contentColumns != null) {
            paramsOut.put("content_columns", contentColumns);
        }

        if (idColumn != null) {
            paramsOut.put("id_column", idColumn);
        }

        if (params != null) {
            paramsOut.putAll(params);
        }

        String astQuery = "CREATE KNOWLEDGE BASE " + project.getName() + "." + name;

        String modelName = model != null ? String.format("%s.%s", model.getProject().getName(), model.getName()) : null;
        String storageName = storage != null ? String.format("%s.%s", storage.getDb().getName(), storage.getName()) : null;

        if (modelName != null || storageName != null || !paramsOut.isEmpty()) {
            astQuery += " USING";
            if (modelName != null) {
                astQuery += " MODEL = " + modelName;
            }
            if (storageName != null) {
                astQuery += " STORAGE = " + storageName;
            }
            if (!paramsOut.isEmpty()) {
                astQuery += " PARAMS = " + new JSONObject(paramsOut).toString();
            }
        }

        api.sqlQuery(astQuery, project.getName());

        return get(name);
    }

    /**
     * Drop a knowledge base by name.
     * @param name The name of the knowledge base to drop.
     */
    public void drop(String name) {
        String astQuery = String.format("DROP KNOWLEDGE BASE %s.%s;", project.getName(), name);
        api.sqlQuery(astQuery, project.getName());
    }

}
