package mindsdb.models;

import lombok.Data;

@Data
public class Model {
    private String name;
    private double accuracy;
    private boolean active;
    private int version;
    private String status;
    private String predict;
    private String mindsdb_version;
    private String error;
    private String fetch_data_query;
    private String created_at;
    private String training_time;
    private String update;
}
