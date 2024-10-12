package mindsdb.client;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Handler;
import mindsdb.models.Project;
import mindsdb.models.skill.Skill;

public class Server extends Project {
    private final Databases databases;
    private final MLEngines mlEngines;
    private final MLHandlers mlHandlers;
    private final DataHandlers dataHandlers;
    // private Agents agents;
    private Skills skills;

    private final Projects projects;
    private final RestAPI api;

    public Server(RestAPI api) {
        super("mindsdb", api);
        this.api = api;
        this.projects = new Projects(this, api);
        this.mlEngines = new MLEngines(api);
        this.mlHandlers = new MLHandlers(api);
        this.dataHandlers = new DataHandlers(api);
        this.databases = new Databases(api);
    }

    // Server status
    public JsonObject status() {
        return api.status();
    }

    // Server string representation
    @Override
    public String toString() {
        return String.format("Server(%s)", api.getUrl());
    }

    public Project getProject(String name) {
        return projects.get(name);
    }

    public List<Project> listProjects() {
        return projects.list();
    }

    public Project createProject(String name) {
        return projects.create(name);
    }

    public void dropProject(String name) {
        projects.drop(name);
    }

    // Engines
    public List<MLEngine> listMLEngines() throws Exception {
        return mlEngines.list();
    }

    public MLEngine getMLEngine(String name) {
        return mlEngines.get(name);
    }

    public MLEngine createMLEngine(String name, Object handler, JSONObject connectionData) {
        return mlEngines.create(name, handler, connectionData);
    }

    public void dropMLEngine(String name) {
        mlEngines.drop(name);
    }

    // Handlers
    // ML Handlers
    public List<Handler> listMLHandlers() {
        return mlHandlers.list();
    }

    public Handler getMLHandler(String name) {
        return mlHandlers.get(name);
    }

    // Data Handlers
    public List<Handler> listDataHandlers() {
        return dataHandlers.list();
    }

    public Handler getDataHandler(String name) {
        return dataHandlers.get(name);
    }

    // Databases
    public List<Database> listDatabases() {
        return databases.list();
    }

    public Database getDatabase(String name) {
        return databases.get(name);
    }

    public Database createDatabase(String name, Object engine, Map<String, String> connectionArgs) {
        return databases.create(name, engine, connectionArgs);
    }

    public void dropDatabase(String name) {
        databases.drop(name);
    }

    // Skills
    public List<Skill> listSkills() {
        return skills.list();
    }

    public RestAPI getApi() {
        return api;
    }

}