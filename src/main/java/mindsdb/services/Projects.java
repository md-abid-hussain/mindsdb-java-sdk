package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Project;

public class Projects {
    private final RestAPI api;
    private final Server server;

    public Projects(Server server, RestAPI api) {
        this.api = api;
        this.server = server;
    }

    private List<String> _listProjects() {
        tech.tablesaw.api.Table response = api
                .sqlQuery("SELECT NAME FROM information_schema.databases WHERE TYPE='project'");
        // return response.getColumn("NAME").stream()
        // .map(Object::toString)
        // .collect(Collectors.toList());

        return response.stream().map(row -> row.getString("NAME")).collect(Collectors.toList());
    }

    public List<Project> list() {
        return _listProjects().stream()
                .map(name -> new Project(name, api))
                .collect(Collectors.toList());
    }

    public Project get(String name) {
        if (!_listProjects().contains(name)) {
            throw new IllegalArgumentException("Project doesn't exist");
        }
        return new Project(name, api);
    }

    public Project create(String name) {
        String astQuery = String.format("CREATE DATABASE %s WITH ENGINE 'mindsdb'", name);
        api.sqlQuery(astQuery);
        return new Project(name, api);
    }

    public void drop(String name) {
        String astQuery = String.format("DROP DATABASE %s", name);
        api.sqlQuery(astQuery);
    }
}
