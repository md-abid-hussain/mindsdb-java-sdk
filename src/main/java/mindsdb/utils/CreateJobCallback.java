package mindsdb.utils;

@FunctionalInterface
public interface CreateJobCallback {
    void execute(String query);
}
