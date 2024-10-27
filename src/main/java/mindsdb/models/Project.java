package mindsdb.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import mindsdb.connectors.RestAPI;
import mindsdb.services.Databases;
import mindsdb.services.Jobs;
import mindsdb.services.KnowledgeBases;
import mindsdb.services.MLEngines;
import mindsdb.services.Models;
import mindsdb.services.Query;
import mindsdb.services.Skills;
import mindsdb.services.Views;

/**
 * Project class for handling project.
 */
@Getter
public class Project {
    private final String name;
    private final RestAPI api;
    private final Jobs jobs;
    private final Views views;
    /**
     * The Models service for managing models.
     */
    public final Models models;
    private final KnowledgeBases knowledgeBases;
    private final Skills skills;
    private final Databases databases;
    private final MLEngines mlEngines;

    /**
     * Constructor for Project
     * @param api - RestAPI object
     * @param name - name of the project
     */
    public Project(RestAPI api, String name) {
        this.name = name;
        this.api = api;
        this.jobs = new Jobs(this, api);
        this.views = new Views(this, api);
        this.models = new Models(this, api);
        this.knowledgeBases = new KnowledgeBases(this, api);
        this.skills = new Skills(this, api);
        this.databases = new Databases(api);
        this.mlEngines = new MLEngines(api);
    }

    /**
     * Get the name of the project
     * 
     * @return name of the project
     */
    public String getName() {
        return name;
    }

    /**
     * Create a Query object
     * 
     * @param sql SQL string
     * @return  Query object
     */
    public Query query(String sql) {
        return new Query(api, sql, name);
    }

    /**
     * Drop a model version
     * 
     * @param modelName Name of the model
     * @param version   Version of the model
     * 
     */
    public void dropModelVersion(String modelName, int version) {
        String astQuery = String.format("DROP MODEL %s.%s", this.name, modelName);
        query(astQuery).fetch();
    }

    @Override
    public String toString() {
        return String.format("Project(%s)", name);
    }

    // Jobs
    /**
     * List all jobs
     * 
     * @return list of Job objects
     */
    public List<Job> listJobs() {
        return jobs.list();
    }

    /**
     * Get a job by name
     * 
     * @param name name of the job
     * @return Job object
     */
    public Job getJob(String name) {
        return jobs.get(name);
    }

    /**
     * Create a job
     * 
     * @param name      name of the job
     * @param queryStr  query string
     * @param startAt   start time
     * @param endAt     end time
     * @param repeatStr repeat string
     * @param repeatMin repeat minutes
     * @return Job object
     */
    public Job createJob(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt, String repeatStr,
            Integer repeatMin) {
        return jobs.create(name, queryStr, startAt, endAt, repeatStr, repeatMin);
    }

    /**
     * Create job with only name
     *
     * @param name name of the job
     * @return Job object
     */
    public Job createJob(String name) {
        return createJob(name, null, null, null, null, null);
    }

    /**
     * Create job with name and query string
     *
     * @param name     name of the job
     * @param queryStr query string
     * @return Job object
     */
    public Job createJob(String name, String queryStr) {
        return createJob(name, queryStr, null, null, null, null);
    }

    /**
     * Create job with name, query string, start time, and end time
     *
     * @param name     name of the job
     * @param queryStr query string
     * @param startAt  start time
     * @param endAt    end time
     * @return Job object
     */
    public Job createJob(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt) {
        return createJob(name, queryStr, startAt, endAt, null, null);
    }

    /**
     * Create job with name, query string, start time, end time, and repeat string
     *
     * @param name      name of the job
     * @param queryStr  query string
     * @param startAt   start time
     * @param endAt     end time
     * @param repeatStr repeat string
     * @return Job object
     */
    public Job createJob(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt, String repeatStr) {
        return createJob(name, queryStr, startAt, endAt, repeatStr, null);
    }

    /**
     * Create job with name and list of query strings
     *
     * @param name     name of the job
     * @param queryStr list of query strings
     * @return Job object
     */
    public Job createJob(String name, List<String> queryStr) {
        return createJob(name, String.join("; ", queryStr), null, null, null, null);
    }

    /**
     * Create job with name, list of query strings, start time, and end time
     *
     * @param name     name of the job
     * @param queryStr list of query strings
     * @param startAt  start time
     * @param endAt    end time
     * @return Job object
     */
    public Job createJob(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt) {
        return createJob(name, String.join("; ", queryStr), startAt, endAt, null, null);
    }

    /**
     * Create job with name, list of query strings, start time, end time, and repeat
     * string
     *
     * @param name      name of the job
     * @param queryStr  list of query strings
     * @param startAt   start time
     * @param endAt     end time
     * @param repeatStr repeat string
     * @return Job object
     */
    public Job createJob(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt,
            String repeatStr) {
        return createJob(name, String.join("; ", queryStr), startAt, endAt, repeatStr, null);
    }

    /**
     * Create job with name, list of query strings, start time, end time, and repeat
     * minutes
     *
     * @param name      name of the job
     * @param queryStr  list of query strings
     * @param startAt   start time
     * @param endAt     end time
     * @param repeatMin repeat minutes
     * @return Job object
     */
    public Job createJob(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt,
            Integer repeatMin) {
        return createJob(name, String.join("; ", queryStr), startAt, endAt, null, repeatMin);
    }

    // Views
    /**
     * List all views
     * 
     * @return list of View objects
     */
    public List<View> listViews() {
        return views.list();
    }

    /**
     * Get a view by name
     * 
     * @param name name of the view
     * @return View object
     */
    public View getView(String name) {
        return views.get(name);
    }

    /**
     * Create a view
     * 
     * @param name  name of the view
     * @param query Query object
     * @return View object
     */
    public View createView(String name, Query query) {
        return views.create(name, query);
    }

    /**
     * Create a view
     * 
     * @param name name of the view
     * @param sql  SQL string
     * @return View object
     */
    public View createView(String name, String sql) {
        return views.create(name, sql);
    }

    /**
     * Drop a view
     * 
     * @param name name of the view
     */
    public void dropView(String name) {
        views.drop(name);
    }

    // Models
    /**
     * List all models
     * 
     * @return list of Model objects
     */
    public List<Model> listModels() {
        return models.listModels(null);
    }

    /**
     * List all models with a specific name and version
     * @param name  name of the model
     * @param version   version of the model
     * @return  list of ModelVersion objects
     */
    public List<ModelVersion> listModelsWithVersion(String name, Integer version) {
        return models.listModelsWithVersion(name, version);
    }

    /**
     * Get a model with a specific name and version
     * @param name  name of the model
     * @param version   version of the model
     * @return  ModelVersion object
     */
    public ModelVersion getModelWithVersion(String name, int version) {
        return models.getModelWithVersion(name, version);
    }

    /**
     * Get a model by name
     * 
     * @param name name of the model
     * @return Model object
     */
    public Model getModel(String name) {
        return models.getModel(name);
    }

    // Knowledgebases
    /**
     * List all knowledge bases
     * 
     * @return list of KnowledgeBase objects
     */
    public List<KnowledgeBase> listKnowledgeBases() {
        return knowledgeBases.list();
    }

    /**
     * Get a knowledge base by name
     * 
     * @param name name of the knowledge base
     * @return KnowledgeBase object
     */
    public KnowledgeBase getKnowledgeBase(String name) {
        return knowledgeBases.get(name);
    }

}
