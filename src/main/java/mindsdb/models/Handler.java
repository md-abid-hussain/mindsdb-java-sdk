package mindsdb.models;

import kong.unirest.core.json.JSONObject;
import lombok.Data;

@Data
public class Handler {
    private String name;
    private String title;
    private String version;
    private String description;
    private JSONObject connectionArgs;
    private boolean importSuccess;
    private String importError;

    // Constructor to handle dynamic parameters
    public Handler(JSONObject kwargs) {
        JSONObject normalizedKwargs = new JSONObject();
        // JSONObject normalizedKwargs = kwargs;

        // Normalize keys to lower case
        for (String key : kwargs.keySet()) {
            switch (key) {
                case "name", "title", "version", "description" ->
                    normalizedKwargs.put(key, (String) kwargs.get(key));
                case "connection_args" ->
                    normalizedKwargs.put(key,
                            kwargs.get(key) != null && kwargs.getString(key).length() > 0 ? kwargs.get(key) : null);
                case "import_success", "import_error" -> {
                    Object value = kwargs.get(key);
                    boolean normalizedValue = value != null && !value.toString().equalsIgnoreCase("false");
                    normalizedKwargs.put(key, normalizedValue);
                }
            }
        }

        if (normalizedKwargs.has("name")) {
            this.name = normalizedKwargs.getString("name");
        }
        if (normalizedKwargs.has("title")) {
            this.title = normalizedKwargs.getString("title");
        }
        if (normalizedKwargs.has("version")) {
            this.version = normalizedKwargs.getString("version");
        }
        if (normalizedKwargs.has("description")) {
            this.description = normalizedKwargs.getString("description");
        }
        if (normalizedKwargs.has("connection_args")) {
            Object connectionArgsValue = normalizedKwargs.get("connection_args");
            if (connectionArgsValue != null) {
                this.connectionArgs = new JSONObject(connectionArgsValue.toString());
            } else {
                this.connectionArgs = null;
            }
        }
        if (normalizedKwargs.has("import_success")) {
            this.importSuccess = normalizedKwargs.getBoolean("import_success");
        }
        if (normalizedKwargs.has("import_error")) {
            Object importErrorValue = normalizedKwargs.get("import_error");
            if (importErrorValue != null) {
                this.importError = importErrorValue.toString();
            } else {
                this.importError = null;
            }
        }
    }
}
