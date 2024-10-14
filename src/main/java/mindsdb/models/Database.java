package mindsdb.models;

import java.util.List;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.services.Tables;

public class Database {
    public String name;
    public String engine;
    public final RestAPI api;
    public final Tables tables;

    public Database(RestAPI api, String name, String engine) {
        this.name = name;
        this.engine = engine;
        this.api = api;
        this.tables = new Tables(this, api);
    }

    public Query query(String sql) {
        return new Query(this.api, sql, this.name);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), this.name);
    }

    public List<Table> listTables() {
        return tables.list();
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public void dropTable(String tableName) {
        this.tables.drop(tableName);
    }
}
