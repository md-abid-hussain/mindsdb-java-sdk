package mindsdb.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;

/**
 * The {@code Handler} class represents a handler with various attributes such
 * as name, title, version, description,
 * connection arguments, import success flag, and import error message. It
 * provides a constructor to initialize these
 * attributes from a JSON object with case-insensitive keys. The class also
 * includes methods to return string
 * representations of the handler.
 * 
 * <p>
 * Attributes:
 * </p>
 * <ul>
 * <li>{@code name}: The name of the handler (String)</li>
 * <li>{@code title}: The title of the handler (String)</li>
 * <li>{@code version}: The version of the handler (String)</li>
 * <li>{@code description}: The description of the handler (String)</li>
 * <li>{@code connectionArgs}: The connection arguments (JsonObject)</li>
 * <li>{@code importSuccess}: The import success flag (Boolean)</li>
 * <li>{@code importError}: The import error message (String)</li>
 * </ul>
 * 
 * <p>
 * Methods:
 * </p>
 * <ul>
 * <li>{@code Handler(JsonObject kwargs)}: Constructs a new Handler instance
 * with the provided JSON object.</li>
 * <li>{@code toString()}: Returns a string representation of the Handler
 * instance.</li>
 * <li>{@code describe()}: Returns a detailed string representation of the
 * Handler instance.</li>
 * </ul>
 */
@Getter
public class Handler {
    private String name;
    private String title;
    private String version;
    private String description;
    private JsonObject connectionArgs;
    private boolean importSuccess;
    private String importError;

    /**
     * Constructs a new Handler instance with the provided JSON object.
     * The constructor normalizes the keys of the provided JSON object to lower case
     * and initializes the Handler's fields based on the normalized keys.
     *
     * @param kwargs A JsonObject containing the initialization parameters.
     *               Expected keys (case-insensitive) are:
     *               <ul>
     *               <li>name: The name of the handler (String)</li>
     *               <li>title: The title of the handler (String)</li>
     *               <li>version: The version of the handler (String)</li>
     *               <li>description: The description of the handler (String)</li>
     *               <li>connection_args: The connection arguments (JsonObject)</li>
     *               <li>import_success: The import success flag (Boolean)</li>
     *               <li>import_error: The import error message (String)</li>
     *               </ul>
     */
    public Handler(JsonObject kwargs) {
        JsonObject normalizedKwargs = new JsonObject();

        // Normalize keys to lower case
        for (String key : kwargs.keySet()) {
            switch (key) {
                case "name", "title", "version", "description" ->
                    normalizedKwargs.addProperty(key, kwargs.get(key).getAsString());
                case "connection_args" -> {
                    JsonElement connectionArgsElement = kwargs.get(key);
                    if (connectionArgsElement != null && connectionArgsElement.isJsonObject()) {
                        normalizedKwargs.add(key, connectionArgsElement.getAsJsonObject());
                    } else {
                        normalizedKwargs.add(key, null);
                    }
                }
                case "import_success", "import_error" -> {
                    JsonElement value = kwargs.get(key);
                    if (value != null && !value.isJsonNull()) {
                        normalizedKwargs.add(key, value);
                    } else {
                        normalizedKwargs.add(key, null);
                    }
                }
            }
        }

        if (normalizedKwargs.has("name")) {
            this.name = normalizedKwargs.get("name").getAsString();
        }
        if (normalizedKwargs.has("title")) {
            this.title = normalizedKwargs.get("title").getAsString();
        }
        if (normalizedKwargs.has("version")) {
            this.version = normalizedKwargs.get("version").getAsString();
        }
        if (normalizedKwargs.has("description")) {
            this.description = normalizedKwargs.get("description").getAsString();
        }
        if (normalizedKwargs.has("connection_args")) {
            JsonElement connectionArgsValue = normalizedKwargs.get("connection_args");
            if (connectionArgsValue != null && connectionArgsValue.isJsonObject()) {
                this.connectionArgs = connectionArgsValue.getAsJsonObject();
            } else {
                this.connectionArgs = null;
            }
        }
        if (normalizedKwargs.has("import_success")) {
            JsonElement importSuccessValue = normalizedKwargs.get("import_success");
            if (importSuccessValue != null && !importSuccessValue.isJsonNull()) {
                this.importSuccess = importSuccessValue.getAsBoolean();
            } else {
                this.importSuccess = false;
            }
        }
        if (normalizedKwargs.has("import_error")) {
            JsonElement importErrorValue = normalizedKwargs.get("import_error");
            if (importErrorValue != null && !importErrorValue.isJsonNull()) {
                this.importError = importErrorValue.getAsString();
            } else {
                this.importError = null;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), this.name);
    }

    /**
     * Returns a string representation of the Handler instance.
     * 
     * @return A string representation of the Handler instance.
     */
    public String describe() {
        return String.format(
                "%s(name=%s, title=%s, version=%s, description=%s, connectionArgs=%s, importSuccess=%s, importError=%s)",
                getClass().getSimpleName(), this.name, this.title, this.version, this.description, this.connectionArgs,
                this.importSuccess, this.importError);
    }
}