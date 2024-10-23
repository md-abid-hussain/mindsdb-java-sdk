package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import tech.tablesaw.api.Table;

public class Handlers {
    protected final RestAPI api;
    protected final String type;

    public Handlers(RestAPI api, String type) {
        this.api = api;
        this.type = type;
    }

    public List<Handler> list() {
        Table response = api.sqlQuery(String.format("SHOW HANDLERS WHERE TYPE='%s';", type));

        return response.stream().map(row -> {
            JsonObject args = new JsonObject();
            for (int i = 0; i < row.columnCount(); i++) {
                args.addProperty(row.columnNames().get(i), row.getString(i));
            }
            return new Handler(args);
        }).collect(Collectors.toList());
    }

    public Handler get(String name) {
        return list().stream()
                .filter(handler -> handler.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Handler doesn't exist"));
    }

}