package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Project;

/**
 * Projects service class for handling projects.
 */
public class Projects {
    private final RestAPI api;
    private final Server server;

    /**
     * Constructor for Projects
     * @param server  - Server object
     * @param api    - RestAPI object
     */
    public Projects(Server server, RestAPI api) {
        this.api = api;
        this.server = server;
    }

    private List<String> _listProjects() {
        tech.tablesaw.api.Table response = api
                .sqlQuery("SELECT NAME FROM information_schema.databases WHERE TYPE='project'");
        return response.stream().map(row -> row.getString("NAME")).collect(Collectors.toList());
    }

    /**
     * List all projects
     * @return List of Project objects
     */
    public List<Project> list() {
        return _listProjects().stream()
                .map(name -> new Project(api, name))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific project by name
     * @param name  - name of the project
     * @return  Project object
     */
    public Project get(String name) {
        if (!_listProjects().contains(name)) {
            throw new IllegalArgumentException("Project doesn't exist");
        }
        return new Project(api, name);
    }

    /**
     * Create a new project
     * @param name - name of the project
     * @return Project object
     */
    public Project create(String name) {
        String astQuery = String.format("CREATE DATABASE %s WITH ENGINE 'mindsdb'", name);
        api.sqlQuery(astQuery);
        return new Project(api, name);
    }

    /**
     * Drop a project
     * @param name - name of the project
     */
    public void drop(String name) {
        String astQuery = String.format("DROP DATABASE %s", name);
        api.sqlQuery(astQuery);
    }
}
