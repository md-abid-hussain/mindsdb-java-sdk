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

public class KnowledgeBases {
    private final Project project;
    private final RestAPI api;

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

    public List<KnowledgeBase> list() {
        return listKnowledgeBases(null);
    }

    public KnowledgeBase get(String name) {
        List<KnowledgeBase> knowledgeBases = listKnowledgeBases(name);
        if (knowledgeBases.isEmpty()) {
            throw new IllegalArgumentException("Knowledge base not found: " + name);
        }
        return knowledgeBases.get(0);
    }

    public KnowledgeBase create(
            String name,
            Model model,
            mindsdb.models.Table storage,
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
        String storageName = storage != null ? String.format("%s.%s", storage.db.getName(), storage.name) : null;

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

    public void drop(String name) {
        String astQuery = String.format("DROP KNOWLEDGE BASE %s.%s;", project.getName(), name);
        api.sqlQuery(astQuery, project.getName());
    }

}
