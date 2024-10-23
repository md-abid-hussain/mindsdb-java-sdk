package mindsdb.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import mindsdb.services.Query;
import mindsdb.utils.CreateJobCallback;
import tech.tablesaw.api.Table;

/**
 * The Job class represents a job with various attributes such as project, name,
 * query string, data, queries, start and end timestamps, next run timestamp,
 * schedule string, and create callback.
 * It provides constructors for creating a job instance and methods for updating
 * the job, refreshing the job, adding a query to the job, getting the job
 * history, and exiting the job.
 * 
 * Attributes:
 * - project: The project associated with the job.
 * - name: The name of the job.
 * - queryString: The query string associated with the job.
 * - data: Additional data for the job in JSON format.
 * - queries: A list of queries associated with the job.
 * - startAt: The timestamp when the job starts.
 * - endAt: The timestamp when the job ends.
 * - nextRunAt: The timestamp when the job runs next.
 * - scheduleStr: The schedule string for the job.
 * - createCallback: The callback function for creating the job.
 * 
 * Methods:
 * - Job(Project project, String name, Map<String, String> data,
 * CreateJobCallback createCallback): Constructs a new Job instance with the
 * specified attributes.
 * - Job(Project project, String name, CreateJobCallback createCallback):
 * Constructs a new Job instance with the specified attributes, without data.
 * - Job(Project project, String name): Constructs a new Job instance with the
 * specified attributes, without data and create callback.
 * - String toString(): Returns a string representation of the job.
 * - void exit(): Exits the job.
 * - void close(): Closes the job.
 * - void refresh(): Refreshes the job.
 * - void addQuery(Query query): Adds a query to the job.
 * - void addQuery(String query): Adds a query to the job.
 * - Table getHistory(): Gets the job history.
 */
@Getter
public class Job implements AutoCloseable {
    private final Project project;
    private final String name;
    private String queryString;
    private Map<String, String> data;
    private List<String> queries;
    private String startAt;
    private String endAt;
    private String nextRunAt;
    private String scheduleStr;
    private CreateJobCallback createCallback;

    /**
     * Constructs a new Job instance with the specified attributes.
     * 
     * @param project        - the project associated with the job
     * @param name           - the name of the job
     * @param data           - additional data for the job in JSON format
     * @param createCallback - the callback function for creating the job
     */
    public Job(Project project, String name, Map<String, String> data, CreateJobCallback createCallback) {
        this.project = project;
        this.name = name;
        this.data = data;
        this.queryString = data != null ? data.get("query") : null;
        this.startAt = data != null ? data.get("start_at") : null;
        this.endAt = data != null ? data.get("end_at") : null;
        this.nextRunAt = data != null ? data.get("next_run_at") : null;
        this.scheduleStr = data != null ? data.get("schedule_str") : null;
        this.queries = new ArrayList<>();
        this.createCallback = createCallback;
    }

    /**
     * Constructs a new Job instance with the specified attributes, without data.
     * 
     * @param project        - the project associated with the job
     * @param name           - the name of the job
     * @param createCallback - the callback function for creating the job
     */
    public Job(Project project, String name, CreateJobCallback createCallback) {
        this(project, name, null, createCallback);
    }

    public Job(Project project, String name) {
        this(project, name, null);
    }

    /**
     * Adds a query to the job.
     * 
     * @param query The query to be added.
     */
    public void addQuery(Query query) {
        if (!query.getDatabase().equals(project.getName())) {
            throw new IllegalArgumentException("Wrong query database: " + query.getDatabase()
                    + ". You could try to use SQL string instead");
        }
        this.queries.add(query.getSql());
    }

    /**
     * Adds a query to the job.
     * 
     * @param sql The SQL query string to be added.
     */
    public void addQuery(String sql) {
        this.queries.add(sql);
    }

    /**
     * Gets the history of the job.
     * 
     * @return The history of the job as a Table.
     */
    public Table getHistory() {
        return project.getApi().sqlQuery("SELECT * FROM log.jobs_history WHERE name = '" + name + "'",
                project.getName());
    }

    /**
     * Refreshes the job by updating its data from the project.
     */
    public void refresh() {
        Job refreshedJob = project.getJob(name);
        if (refreshedJob != null && refreshedJob.getData() != null) {
            if (this.data == null) {
                this.data = refreshedJob.getData();
            } else {
                this.data.putAll(refreshedJob.getData());
            }
        }
    }

    /**
     * Exits the job by executing the create callback with the queries.
     */
    public void exit() {
        if (queries.isEmpty()) {
            throw new IllegalStateException("No queries were added to job");
        }
        for (String query : queries) {
            createCallback.execute(query);
        }
        project.getJob(name);
    }

    /**
     * Closes the job by executing the create callback with the queries.
     * 
     */
    @Override
    public void close() {
        this.exit();
    }

    @Override
    public String toString() {
        return String.format("%s(%s, query='%s')", this.getClass().getSimpleName(), name, queryString);
    }
}