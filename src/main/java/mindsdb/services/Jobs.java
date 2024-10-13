package mindsdb.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Job;
import mindsdb.models.Project;
import mindsdb.utils.CreateJobCallback;
import mindsdb.utils.DataFrame;

public class Jobs {

    private final Project project;
    private final RestAPI api;

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

        DataFrame response = api.sqlQuery(astQuery, this.project.getName());

        return response.getRows().stream()
                .map(row -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("query", row.get("QUERY") != null ? row.get("QUERY").toString() : null);
                    data.put("start_at", row.get("START_AT") != null ? row.get("START_AT").toString() : null);
                    data.put("end_at", row.get("END_AT") != null ? row.get("END_AT").toString() : null);
                    data.put("schedule_str",
                            row.get("SCHEDULE_STR") != null ? row.get("SCHEDULE_STR").toString() : null);
                    Job job = new Job(this.project, row.get("NAME").toString(), data, null);
                    return job;
                })
                .collect(Collectors.toList());
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
                .filter(job -> job.name.equals(name))
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
        String endStr = (endAt != null) ? " END " + endAt.format(formatter) : null;
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

            // String astQuery = String.format("CREATE JOB %s WITH QUERY %s START %s END %s
            // REPEAT %s",
            // name, query, startStr, endStr, finalRepeatStr);
            api.sqlQuery(astQuery.toString(), project.getName());
        };

        if (queryStr == null) {
            Job job = new Job(project, name, createCallback);
            return job;
        } else {
            createCallback.execute(queryStr);
            List<Job> jobs = this.listJobs(name);

            System.out.println("jobs " + jobs);
            if (jobs.size() == 1) {
                return jobs.get(0);
            } else {
                return null;
            }
        }
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
