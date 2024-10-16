package mindsdb.services;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Handler;
import mindsdb.models.MLEngine;
import mindsdb.models.Project;
import mindsdb.models.skill.Skill;

public class Server extends Project {
    public final Databases databases;
    public final MLEngines mlEngines;
    public final MLHandlers mlHandlers;
    public final DataHandlers dataHandlers;
    // public Agents agents;
    public final Skills skills;

    public final Projects projects;
    // public final RestAPI api;

    public Server(RestAPI api) {
        super(null, api, "mindsdb");
        // this.api = api;
        this.projects = new Projects(this, api);
        this.mlEngines = new MLEngines(api);
        this.mlHandlers = new MLHandlers(api);
        this.dataHandlers = new DataHandlers(api);
        this.databases = new Databases(api);
        this.skills = new Skills(this, api);
    }

    // Server status
    /**
     * Get the status of the server
     * 
     * @return JsonObject with the server status
     */
    public JsonObject status() {
        return api.status();
    }

    // Server string representation
    @Override
    public String toString() {
        return String.format("Server(%s)", api.getUrl());
    }

    /**
     * Get A project
     * 
     * @param name Name of the project
     * @return Project
     */
    public Project getProject(String name) {
        return projects.get(name);
    }

    /**
     * List all projects
     * 
     * @return List of projects
     */
    public List<Project> listProjects() {
        return projects.list();
    }

    /**
     * Create a project
     * 
     * @param name Name of the project
     * @return Project
     */
    public Project createProject(String name) {
        return projects.create(name);
    }

    /**
     * Drop a project
     * 
     * @param name Name of the project
     */
    public void dropProject(String name) {
        projects.drop(name);
    }

    // Engines
    /**
     * List all ML engines
     * 
     * @return List of ML engines
     */
    public List<MLEngine> listMLEngines() {
        return mlEngines.list();
    }

    /**
     * Get an ML engine by name
     * 
     * @param name Name of the ML engine
     * @return MLEngine
     */
    public MLEngine getMLEngine(String name) {
        return mlEngines.get(name);
    }

    /**
     * Create an ML engine
     * 
     * @param name           Name of the ML engine
     * @param handler        Handler object
     * @param connectionData Connection data
     * @return MLEngine
     */
    public MLEngine createMLEngine(String name, Handler handler, JSONObject connectionData) {
        return mlEngines.create(name, handler, connectionData);
    }

    /**
     * Create an ML engine
     * 
     * @param name           Name of the ML engine
     * @param handlerName    Name of the handler
     * @param connectionData Connection data
     * @return MLEngine
     */
    public MLEngine createMLEngine(String name, String handlerName, JSONObject connectionData) {
        return mlEngines.create(name, handlerName, connectionData);
    }

    /**
     * Create an ML engine
     * 
     * @param name    Name of the ML engine
     * @param handler Handler object
     * @return MLEngine
     */
    public MLEngine createMLEngine(String name, Handler handler) {
        return mlEngines.create(name, handler);
    }

    /**
     * Create an ML engine
     * 
     * @param name        Name of the ML engine
     * @param handlerName Name of the handler
     * @return MLEngine
     */
    public MLEngine createMLEngine(String name, String handlerName) {
        return mlEngines.create(name, handlerName);
    }

    /**
     * Drop an ML engine
     * 
     * @param name Name of the ML engine
     */
    public void dropMLEngine(String name) {
        mlEngines.drop(name);
    }

    // Handlers
    // ML Handlers
    /**
     * List all ML handlers
     * 
     * @return List of ML handlers
     */
    public List<Handler> listMLHandlers() {
        return mlHandlers.list();
    }

    /**
     * Get an ML handler by name
     * 
     * @param name Name of the ML handler
     * @return Handler
     */
    public Handler getMLHandler(String name) {
        return mlHandlers.get(name);
    }

    // Data Handlers
    /**
     * List all data handlers
     * 
     * @return List of data handlers
     */
    public List<Handler> listDataHandlers() {
        return dataHandlers.list();
    }

    /**
     * Get a data handler by name
     * 
     * @param name Name of the data handler
     * @return Handler
     */
    public Handler getDataHandler(String name) {
        return dataHandlers.get(name);
    }

    // Databases
    /**
     * List all databases
     * 
     * @return List of databases
     */
    public List<Database> listDatabases() {
        return databases.list();
    }

    /**
     * Get a database by name
     * 
     * @param name Name of the database
     * @return Database
     */
    public Database getDatabase(String name) {
        return databases.get(name);
    }

    /**
     * Create a database
     * 
     * @param name           Name of the database
     * @param engine         Engine object
     * @param connectionArgs Connection arguments
     * @return Database
     */
    public Database createDatabase(String name, String engine, Map<String, String> connectionArgs) {
        return databases.create(name, engine, connectionArgs);
    }

    /**
     * Create a database
     * 
     * @param name           Name of the database
     * @param engine         Engine object
     * @param connectionArgs Connection arguments
     * @return Database
     */
    public Database createDatabase(String name, Handler engine, Map<String, String> connectionArgs) {
        return databases.create(name, engine.getName(), connectionArgs);
    }

    /**
     * Drop a database
     * 
     * @param name Name of the database
     */
    public void dropDatabase(String name) {
        databases.drop(name);
    }

    // Skills
    /**
     * List all skills
     * 
     * @return List of skills
     */
    public List<Skill> listSkills() {
        return skills.list();
    }

    /**
     * Get a skill by name
     * 
     * @param name Name of the skill
     * @return Skill
     */
    public Skill getSkill(String name) {
        return skills.get(name);
    }

    /**
     * Create a skill
     * 
     * @param name   Name of the skill
     * @param type   Type of the skill
     * @param params Parameters of the skill
     * @return Skill
     */
    public Skill createSkill(String name, String type, JsonObject params) {
        return skills.create(name, type, params);
    }

    /**
     * Update a skill
     * 
     * @param name        Name of the skill
     * @param updateSkill Skill object
     * @return Skill
     */
    public Skill updateSkill(String name, Skill updateSkill) {
        return skills.update(name, updateSkill);
    }

    /**
     * Drop a skill
     * 
     * @param name Name of the skill
     */
    public void dropSkill(String name) {
        skills.drop(name);
    }

    public RestAPI getApi() {
        return api;
    }

}