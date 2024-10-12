package mindsdb.models.skill;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Skill {
    public final String name;
    public final String type;
    public final JsonObject params;

    public Skill(String name, String type, JsonObject params) {
        this.name = name;
        this.type = type;
        this.params = (params != null) ? params : new JsonObject();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Skill other = (Skill) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        return true;
    }

    public static Skill fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String type = json.get("type").getAsString();
        JsonObject params = json.getAsJsonObject("params");
        return new Skill(name, type, params);
    }

    protected static JsonObject createParams(String database, List<String> tables, String description) {
        JsonObject params = new JsonObject();
        params.addProperty("database", database);
        if (tables != null) {
            JsonArray tablesJsonArray = new JsonArray();
            for (String table : tables) {
                tablesJsonArray.add(table);
            }
            params.add("tables", tablesJsonArray);
        }
        params.addProperty("description", description);
        return params;
    }
}
