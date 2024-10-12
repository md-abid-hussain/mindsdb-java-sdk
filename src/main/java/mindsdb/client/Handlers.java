package mindsdb.client;

import java.util.List;
import java.util.stream.Collectors;

import kong.unirest.core.json.JSONObject;
import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import mindsdb.utils.DataFrame;

public class Handlers {
    protected final RestAPI api;
    protected final String type;

    public Handlers(RestAPI api, String type) {
        this.api = api;
        this.type = type;
    }

    public List<Handler> list() {
        DataFrame response = api.sqlQuery(String.format("SHOW HANDLERS WHERE TYPE='%s';", type));
        return response.getRows().stream()
                .map(row -> {
                    JSONObject args = new JSONObject(row);
                    return new Handler(args);
                })
                .collect(Collectors.toList());
    }

    public Handler get(String name) {
        return list().stream()
                .filter(handler -> handler.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Handler doesn't exist"));
    }

}