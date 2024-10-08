package mindsdb.models;

import lombok.Data;

@Data
public class Database {
    private String name;
    private String type;
    private String engine;
}
