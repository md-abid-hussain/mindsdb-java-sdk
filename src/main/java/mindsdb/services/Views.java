package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Project;
import mindsdb.models.View;
import tech.tablesaw.api.Table;

/**
 * Views service class for handling views.
 */
public class Views {

    private Project project;
    private RestAPI api;

    /**
     * Constructor for Views
     * @param project   - Project object
     * @param api    - RestAPI object
     */
    public Views(Project project, RestAPI api) {
        this.project = project;
        this.api = api;
    }

    private List<String> listViews() {
        Table response = api.objectsTree(project.getName());

        return response.where(response.stringColumn("type").isEqualTo("view")).stringColumn("name").asList().stream()
                .map(Object::toString).collect(Collectors.toList());
    }

    /**
     * List all views
     * @return List of View objects
     */
    public List<View> list() {
        return listViews().stream()
                .map(viewName -> new View(project, viewName))
                .toList();
    }

    /**
     * Create a new view
     * @param name  - name of the view
     * @param sql   - SQL query for the view
     * @return  View object
     */
    public View create(String name, String sql) {
        StringBuilder astQuery = new StringBuilder("CREATE VIEW ");
        astQuery.append(name);

        astQuery.append(" AS (").append(sql).append(");");

        this.project.query(astQuery.toString()).fetch();
        return new View(project, name);
    }
    
    /**
     * Create a new view
     * @param name  - name of the view
     * @param query     - Query object for the view
     * @return  View object
     */
    public View create(String name, Query query) {
        return create(name, query.getSql());
    }

    /**
     * Drop a view
     * @param name - name of the view
     */
    public void drop(String name) {
        String astQuery = String.format("DROP VIEW %s", name);
        api.sqlQuery(astQuery);
    }

    /**
     * Get a view by name
     * @param name  - name of the view
     * @return  View object
     */
    public View get(String name) {
        return listViews().stream()
                .filter(viewName -> viewName.equals(name))
                .map(viewName -> new View(project, viewName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("View doesn't exist"));
    }

}
