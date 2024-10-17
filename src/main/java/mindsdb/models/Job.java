package mindsdb.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mindsdb.services.Query;
import mindsdb.utils.CreateJobCallback;
import tech.tablesaw.api.Table;

public class Job implements AutoCloseable {
    public final Project project;
    public final String name;
    public String queryString;
    public Map<String, String> data;
    public List<String> queries;
    public String startAt;
    public String endAt;
    public String nextRunAt;
    public String scheduleStr;
    public CreateJobCallback createCallback;

    public Job(Project project, String name, Map<String, String> data, CreateJobCallback createCallback) {
        this.project = project;
        this.name = name;
        this.data = data;
        this.createCallback = createCallback;
        this.queries = new ArrayList<>();

        if (data != null) {
            update(data);
        }
    }

    public Job(Project project, String name, CreateJobCallback createCallback) {
        this(project, name, null, createCallback);
    }

    private void update(Map<String, String> data) {
        this.queryString = data.get("query");
        this.startAt = data.get("start_at");
        this.endAt = data.get("end_at");
        this.nextRunAt = data.get("next_run_at");
        this.scheduleStr = data.get("schedule_str");
    }

    @Override
    public String toString() {
        return String.format("%s(%s, query='%s')", this.getClass().getSimpleName(), name, queryString);
    }

    public void exit() {
        if (queries.isEmpty()) {
            throw new IllegalStateException("No queries were added to job");
        }

        String queryStr = String.join("; ", queries);
        createCallback.execute(queryStr);
        refresh();
    }

    @Override
    public void close() throws Exception {
        this.exit();
    }

    public void refresh() {
        Job job = project.getJob(name);
        update(job.data);
    }

    /**
     * Add a query to the job
     * 
     * @param query Query object
     * @return Job object
     */
    public Job addQuery(Query query) {
        if (query.database != null && !query.database.equals(project.getName())) {
            throw new IllegalArgumentException("Wrong query database: " + query.database
                    + ". You could try to use SQL string instead");
        }
        queries.add(query.sql);
        return this;
    }

    /**
     * Add a query to the job
     * 
     * @param query SQL string
     * @return Job object
     */
    public Job addQuery(String query) {
        queries.add(query);
        return this;
    }

    /**
     * Get the job history
     * 
     * @return Table object with the job history
     */
    public Table getHistory() {
        String astQuery = String.format("SELECT * FROM log.jobs_history WHERE name = '%s'", name);
        return project.api.sqlQuery(astQuery, project.getName());
    }

}
