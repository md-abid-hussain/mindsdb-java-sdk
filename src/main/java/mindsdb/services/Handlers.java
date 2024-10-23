package mindsdb.services;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import tech.tablesaw.api.Table;

/**
 * The {@code Handlers} class provides methods to interact with handlers of a specific type.
 */
public class Handlers {
    /**
     * The API instance used to interact with the backend.
     */
    protected final RestAPI api;

    /**
     * The type of handlers this instance manages.
     */
    protected final String type;

    /**
     * Constructs a new {@code Handlers} instance with the provided API and handler type.
     *
     * @param api  The API instance used to interact with the backend.
     * @param type The type of handlers this instance manages.
     */
    public Handlers(RestAPI api, String type) {
        this.api = api;
        this.type = type;
    }

    /**
     * Lists all handlers of the specified type.
     *
     * @return A list of handlers.
     */
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

    /**
     * Gets a handler by its name.
     *
     * @param name The name of the handler.
     * @return The handler with the specified name.
     * @throws IllegalArgumentException if the handler does not exist.
     */
    public Handler get(String name) {
        return list().stream()
                .filter(handler -> handler.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Handler doesn't exist"));
    }

}