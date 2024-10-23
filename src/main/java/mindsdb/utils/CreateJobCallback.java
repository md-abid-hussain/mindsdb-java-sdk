package mindsdb.utils;

/**
 * The CreateJobCallback interface provides a method for creating a job with a
 */
@FunctionalInterface
public interface CreateJobCallback {

    /**
     * Executes the specified query.
     * @param query The query to be executed.
     */
    void execute(String query);
}
