package mindsdb.models;

import java.time.LocalDateTime;
import java.util.List;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Jobs;
import mindsdb.services.Query;

public class Project {
    private final String name;
    public final RestAPI api;
    public Jobs jobs;

    public Project(String name, RestAPI api) {
        this.name = name;
        this.api = api;
        this.jobs = new Jobs(this, api);
    }

    public String getName() {
        return name;
    }

    public Query query(String sql) {
        return new Query(api, sql, name);
    }

    public void dropModelVersion(String modelName, int version) {
        String astQuery = String.format("DROP PREDICTOR %s.%d", modelName, version);
        query(astQuery).fetch();
    }

    @Override
    public String toString() {
        return String.format("Project(%s)", name);
    }

    // Jobs
    public List<Job> listJobs() {
        return jobs.list();
    }

    public Job getJob(String name) {
        return jobs.get(name);
    }

    public Job createJob(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt, String repeatStr,
            Integer repeatMin) {
        return jobs.create(name, queryStr, startAt, endAt, repeatStr, repeatMin);
    }

}
