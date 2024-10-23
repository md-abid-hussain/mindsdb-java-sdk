package mindsdb.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Job;
import mindsdb.models.Project;
import mindsdb.utils.CreateJobCallback;
import tech.tablesaw.api.Table;

/**
 * The Jobs class provides methods to list, get, create, and drop jobs in a project.
 */
public class Jobs {

    private final Project project;
    private final RestAPI api;

    /**
     * Constructs a new Jobs instance with the provided project and API.
     * @param project The project associated with the jobs. 
     * @param api The API instance used to interact with the backend.
     */
    public Jobs(Project project, RestAPI api) {
        this.project = project;
        this.api = api;
    }

    /**
     * List jobs in project
     * 
     * @param name job name
     * @return list of Job objects
     */
    private List<Job> listJobs(String name) {
        String astQuery = String.format("SELECT * FROM %s.jobs", project.getName());

        if (name != null) {
            astQuery += String.format(" WHERE name='%s';", name);
        }

        Table response = api.sqlQuery(astQuery, this.project.getName());

        if (response == null) {
            return new ArrayList<>();
        }

        return response.stream().map(row -> {
            Map<String, String> data = new HashMap<>();
            data.put("query", row.getString("QUERY"));
            data.put("start_at", row.getString("START_AT"));
            data.put("end_at", row.getString("END_AT"));
            data.put("schedule_str", row.getString("SCHEDULE_STR"));
            Job job = new Job(this.project, row.getString("NAME"), data, null);
            return job;
        }).collect(Collectors.toList());
    }

    /**
     * Show list of jobs in project
     *
     * @return list of Job objects
     */
    public List<Job> list() {
        return listJobs(null);
    }

    /**
     * Get job by name
     *
     * @param name job name
     * @return Job object
     */
    public Job get(String name) {
        return listJobs(name).stream()
                .filter(job -> job.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Job doesn't exist"));
    }

    /**
     * Create job in project
     *
     * @param name      name of the job
     * @param queryStr  query string
     * @param startAt   start time
     * @param endAt     end time
     * @param repeatStr repeat string
     * @param repeatMin repeat minutes
     * @return Job object
     */
    public Job create(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt, String repeatStr,
            Integer repeatMin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startStr = (startAt != null) ? " START " + String.format("'%s'", startAt.format(formatter)) : null;
        String endStr = (endAt != null) ? " END " + String.format("'%s'", endAt.format(formatter)) : null;
        final String finalRepeatStr = (repeatMin != null) ? " EVERY " + repeatMin + " minutes"
                : repeatStr != null ? " EVERY " + repeatStr : null;

        CreateJobCallback createCallback = (query) -> {
            StringBuilder astQuery = new StringBuilder("CREATE JOB " + name);
            if (query != null) {
                astQuery.append(" (").append(query).append(")");
            }

            if (startStr != null) {
                astQuery.append(startStr);
            }

            if (endStr != null) {
                astQuery.append(endStr);
            }

            if (finalRepeatStr != null) {
                astQuery.append(finalRepeatStr);
            }
            api.sqlQuery(astQuery.toString(), project.getName());
        };

        if (queryStr == null) {
            Job job = new Job(project, name, createCallback);
            return job;
        } else {
            createCallback.execute(queryStr);
            List<Job> jobs = this.listJobs(name);

            if (jobs.size() == 1) {
                return jobs.get(0);
            } else {
                return null;
            }
        }
    }

    /**
     * Create job with only name
     *
     * @param name name of the job
     * @return Job object
     */
    public Job create(String name) {
        return create(name, null, null, null, null, null);
    }

    /**
     * Create job with name and query string
     *
     * @param name     name of the job
     * @param queryStr query string
     * @return Job object
     */
    public Job create(String name, String queryStr) {
        return create(name, queryStr, null, null, null, null);
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
    public Job create(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt) {
        return create(name, queryStr, startAt, endAt, null, null);
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
    public Job create(String name, String queryStr, LocalDateTime startAt, LocalDateTime endAt, String repeatStr) {
        return create(name, queryStr, startAt, endAt, repeatStr, null);
    }

    /**
     * Create job with name and list of query strings
     *
     * @param name     name of the job
     * @param queryStr list of query strings
     * @return Job object
     */
    public Job create(String name, List<String> queryStr) {
        return create(name, String.join("; ", queryStr), null, null, null, null);
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
    public Job create(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt) {
        return create(name, String.join("; ", queryStr), startAt, endAt, null, null);
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
    public Job create(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt,
            String repeatStr) {
        return create(name, String.join("; ", queryStr), startAt, endAt, repeatStr, null);
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
    public Job create(String name, List<String> queryStr, LocalDateTime startAt, LocalDateTime endAt,
            Integer repeatMin) {
        return create(name, String.join("; ", queryStr), startAt, endAt, null, repeatMin);
    }

    /**
     * Drop job from project
     *
     * @param name name of the job
     */
    public void drop(String name) {
        String astQuery = String.format("DROP JOB %s", name);
        api.sqlQuery(astQuery, project.getName());
    }
}
