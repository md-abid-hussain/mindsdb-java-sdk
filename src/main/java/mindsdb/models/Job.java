package mindsdb.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mindsdb.services.Query;
import mindsdb.utils.CreateJobCallback;
import mindsdb.utils.DataFrame;

public class Job implements AutoCloseable {
    public final Project project;
    public final String name;
    public String queryString;
    public Map<String, String> data;
    private List<String> queries;
    private String startAt;
    private String endAt;
    private String nextRunAt;
    private String scheduleStr;
    private CreateJobCallback createCallback;

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

    // public Job enter() {

    // System.out.println(1);
    // if (createCallback == null) {
    // throw new IllegalStateException("The job is already created and can't be used
    // to create context."
    // + " To be able to use context: create job without 'query_str' parameter: ");
    // }
    // ContextManager.setSaving("job-" + name);
    // return this;
    // }

    public void exit() {
        // ContextManager.setSaving(null);
        if (queries.isEmpty()) {
            throw new IllegalStateException("No queries were added to job");
        }

        String queryStr = String.join("; ", queries);
        // System.out.println("queryStr: " + queryStr);
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

    // public void addQuery(Object query) {
    // if (query instanceof Query) {
    // Query q = (Query) query;
    // if (q.database != null && !q.database.equals(project.getName())) {
    // throw new IllegalArgumentException("Wrong query database: " + q.database
    // + ". You could try to use SQL string instead");
    // }
    // queries.add(q.sql);
    // } else if (query instanceof String) {
    // queries.add((String) query);
    // } else {
    // throw new IllegalArgumentException("Unable to use add this object as a query:
    // " + query
    // + ". Try to use SQL string instead");
    // }
    // }

    public Job addQuery(Query query) {
        if (query.database != null && !query.database.equals(project.getName())) {
            throw new IllegalArgumentException("Wrong query database: " + query.database
                    + ". You could try to use SQL string instead");
        }
        queries.add(query.sql);
        return this;
    }

    public Job addQuery(String query) {
        queries.add(query);
        return this;
    }

    public DataFrame getHistory() {
        String astQuery = String.format("SELECT * FROM log.jobs_history WHERE name = '%s'", name);
        return project.api.sqlQuery(astQuery, project.getName());
    }

}
