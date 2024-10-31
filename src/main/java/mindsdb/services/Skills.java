package mindsdb.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Project;
import mindsdb.models.skill.SQLSkill;
import mindsdb.models.skill.Skill;

/**
 * The Skills class provides methods for managing skills.
 */
public class Skills {
    private final RestAPI api;
    private final Project project;

    /**
     * Constructs a new Skills instance with the specified project and API.
     * @param project   The project associated with the skills. 
     * @param api    The API instance used to interact with the backend.
     */
    public Skills(Project project, RestAPI api) {
        this.api = api;
        this.project = project;
    }

    /**
     * List all skills in the project.
     * @return  List of skills.
     */
    public List<Skill> list() {
        List<Skill> skills = new ArrayList<>();
        JsonArray jsonSkills = api.skills(project.getName());

        for (JsonElement jsonSkill : jsonSkills) {
            JsonObject skill = jsonSkill.getAsJsonObject();
            Skill s = Skill.fromJson(skill);
            skills.add(s);
        }

        return skills;
    }

    /**
     * Get a skill by name.
     * @param name  The name of the skill.
     * @return  The skill with the specified name.
     */
    public Skill get(String name) {
        JsonObject jsonSkill = api.skill(project.getName(), name);
        return Skill.fromJson(jsonSkill);
    }

    /**
     * Create a new skill with the specified name, type, and parameters.
     * @param name  The name of the skill.
     * @param type      The type of the skill.
     * @param params    The parameters of the skill.
     * @return  The created skill.
     */
    public Skill create(String name, String type, JsonObject params) {
        api.createSkill(this.project.getName(), name, type, params);

        if (type.equals("sql")) {
            JsonArray tablesJsonArray = params.getAsJsonArray("tables");
            List<String> tables = new ArrayList<>();
            for (JsonElement tableElement : tablesJsonArray) {
                tables.add(tableElement.getAsString());
            }
            String database = params.get("database").getAsString();
            String description = params.has("description") ? params.get("description").getAsString() : null;
            return new SQLSkill(name, tables, database, description);
        }

        Map<String, Object> paramsMap = new Gson().fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        return new Skill(name, type, paramsMap);
    }

    /**
     * Create a new skill with the specified name, type, and parameters.
     * @param name  The name of the skill.
     * @param type    The type of the skill.    
     * @param params    The parameters of the skill.
     * @return  The created skill.
     */
    public Skill create(String name, String type, Map<String, Object> params) {
        JsonObject jsonParams = new Gson().toJsonTree(params).getAsJsonObject();

        return create(name, type, jsonParams);
    }

    /**
     * Update a skill with the specified name and updated skill.
     * @param name  The name of the skill.
     * @param updateSkill   The updated skill.
     * @return  The updated skill.
     */
    public Skill update(String name, Skill updateSkill) {
        JsonObject params = new JsonObject();
        for (Map.Entry<String, Object> entry : updateSkill.getParams().entrySet()) {
            params.add(entry.getKey(), new Gson().toJsonTree(entry.getValue()));
        }
        api.updateSkill(this.project.getName(), name, updateSkill.getName(), updateSkill.getType(), params);
        return updateSkill;
    }

    /**
     * Drop a skill with the specified name.
     * @param name  The name of the skill.
     */
    public void drop(String name) {
        api.deleteSkill(this.project.getName(), name);
    }
}
