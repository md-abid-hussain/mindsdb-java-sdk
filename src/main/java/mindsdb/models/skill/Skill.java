package mindsdb.models.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The Skill class represents a skill with a name, type, and parameters.
 * It provides constructors for creating a skill instance and methods for
 * converting JSON data to a Skill object.
 * 
 * Attributes:
 * - name: The name of the skill.
 * - type: The type of the skill.
 * - params: The parameters of the skill.
 * 
 * Methods:
 * - Skill(String name, String type, Map<String, Object> params):
 * Constructs a new Skill instance with the specified name, type, and
 * parameters.
 * - static Skill fromJson(JsonObject json): Converts JSON data to a Skill
 * object.
 */
public class Skill {
    public final String name;
    public final String type;
    public final Map<String, Object> params;

    public Skill(String name, String type, Map<String, Object> params) {
        this.name = name;
        this.type = type;
        this.params = (params != null) ? params : new HashMap<>();
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

    @Override
    public String toString() {
        return "Skill(name=" + name + ")";
    }

    public String describe() {
        return "Skill(name=" + name + ", type=" + type + ", params=" + params + ")";
    }

    /**
     * Converts JSON data to a Skill object.
     * 
     * @param json The JSON data to convert.
     * @return The Skill object created from the JSON data.
     */
    public static Skill fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String type = json.get("type").getAsString();
        JsonObject paramsJson = json.getAsJsonObject("params");
        Map<String, Object> params = new Gson().fromJson(paramsJson, new TypeToken<Map<String, Object>>() {
        }.getType());
        return new Skill(name, type, params);
    }

    protected static Map<String, Object> createParams(String database, List<String> tables, String description) {
        JsonObject paramsJson = new JsonObject();
        paramsJson.addProperty("database", database);
        if (tables != null) {
            JsonArray tablesJsonArray = new JsonArray();
            for (String table : tables) {
                tablesJsonArray.add(table);
            }
            paramsJson.add("tables", tablesJsonArray);
        }
        paramsJson.addProperty("description", description);
        return new Gson().fromJson(paramsJson, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

}
