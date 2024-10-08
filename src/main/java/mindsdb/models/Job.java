package mindsdb.models;

import lombok.Data;

@Data
public class Job {
    private String name;
    private String query;
    private String ifQuery;
    private String startAt;
    private String endAt;
    private String scheduleStr;
}
