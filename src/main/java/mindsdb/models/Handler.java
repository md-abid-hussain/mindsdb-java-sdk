package mindsdb.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;

@Getter
public class Handler {
    private String name;
    private String title;
    private String version;
    private String description;
    private JsonObject connectionArgs;
    private boolean importSuccess;
    private String importError;

    // Constructor to handle dynamic parameters
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

    public String describe() {
        return String.format(
                "%s(name=%s, title=%s, version=%s, description=%s, connectionArgs=%s, importSuccess=%s, importError=%s)",
                getClass().getSimpleName(), this.name, this.title, this.version, this.description, this.connectionArgs,
                this.importSuccess, this.importError);
    }
}