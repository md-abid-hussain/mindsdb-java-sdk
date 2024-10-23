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

/**
 * The Server class extends the Project class and provides various services such as managing databases,
 * ML engines, ML handlers, data handlers, agents, and skills. It also provides methods to interact with
 * these services.
 */
public class Server extends Project {
    /**
     * The Databases service for managing databases.
     */
    public final Databases databases;

    /**
     * The MLEngines service for managing machine learning engines.
     */
    public final MLEngines mlEngines;

    /**
     * The MLHandlers service for managing machine learning handlers.
     */
    public final MLHandlers mlHandlers;

    /**
     * The DataHandlers service for managing data handlers.
     */
    public final DataHandlers dataHandlers;

    /**
     * The Agents service for managing agents.
     */
    public final Agents agents;

    /**
     * The Skills service for managing skills.
     */
    public final Skills skills;

    /**
     * The Projects service for managing projects.
     */
    public final Projects projects;

    /**
     * Constructs a new Server instance with the specified RestAPI.
     *
     * @param api The RestAPI instance to be used by the server.
     */
    public Server(RestAPI api) {
        super(api, "mindsdb");
        this.projects = new Projects(this, api);
        this.mlEngines = new MLEngines(api);
        this.mlHandlers = new MLHandlers(api);
        this.dataHandlers = new DataHandlers(api);
        this.databases = new Databases(api);
        this.skills = new Skills(this, api);
        this.agents = new Agents(this, api);
    }

    // Server status
    /**
     * Get the status of the server
     * 
     * @return JsonObject with the server status
     */
    public JsonObject status() {
        return getApi().status();
    }

    // Server string representation
    @Override
    public String toString() {
        return String.format("Server(%s)", getApi().getUrl());
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
    public Skill createSkill(String name, String type, Map<String, Object> params) {
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
}