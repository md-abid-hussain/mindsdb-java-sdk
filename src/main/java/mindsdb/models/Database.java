package mindsdb.models;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;

public class Database {
    // private Server server;
    public String name;
    private String engine;
    public final RestAPI api;

    public Database(RestAPI api, String name, String engine) {
        // this.server = server;
        this.name = name;
        this.engine = engine;
        this.api = api;
    }

    public Query query(String sql) {
        return new Query(this.api, sql, this.name);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), this.name);
    }
}
