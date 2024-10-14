package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Project;
import mindsdb.models.View;
import tech.tablesaw.api.Table;

public class Views {

    public Project project;
    public RestAPI api;

    public Views(Project project, RestAPI api) {
        this.project = project;
        this.api = api;
    }

    private List<String> listViews() {
        Table response = api.objectsTree(project.getName());

        return response.where(response.stringColumn("type").isEqualTo("view")).stringColumn("name").asList().stream()
                .map(Object::toString).collect(Collectors.toList());
    }

    public List<View> list() {
        System.out.println(listViews());
        return listViews().stream()
                .map(viewName -> new View(project, viewName))
                .toList();
    }

    public View create(String name, String sql) {
        StringBuilder astQuery = new StringBuilder("CREATE VIEW ");
        astQuery.append(name);

        astQuery.append(" AS (").append(sql).append(");");

        this.project.query(astQuery.toString()).fetch();
        return new View(project, name);
    }

    public View create(String name, Query query) {
        return create(name, query.sql);
    }

    // public View create(String name, Query query) {
    // return create(name, query, null);
    // }

    // public View create(String name, String sql) {
    // return create(name, sql, null);
    // }

    public void drop(String name) {
        String astQuery = String.format("DROP VIEW %s", name);
        api.sqlQuery(astQuery);
    }

    public View get(String name) {
        return listViews().stream()
                .filter(viewName -> viewName.equals(name))
                .map(viewName -> new View(project, viewName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("View doesn't exist"));
    }

}
