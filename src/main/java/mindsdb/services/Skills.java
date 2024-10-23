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

public class Skills {
    private final RestAPI api;
    private final Project project;

    public Skills(Project project, RestAPI api) {
        this.api = api;
        this.project = project;
    }

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

    public Skill get(String name) {
        JsonObject jsonSkill = api.skill(project.getName(), name);
        return Skill.fromJson(jsonSkill);
    }

    public Skill create(String name, String type, JsonObject params) {
        api.createSkill(this.project.getName(), name, type, params);

        if (type.equals("sql")) {
            JsonArray tablesJsonArray = params.getAsJsonArray("tables");
            List<String> tables = new ArrayList<>();
            for (JsonElement tableElement : tablesJsonArray) {
                tables.add(tableElement.getAsString());
            }
            String database = params.get("database").getAsString();
            String description = params.get("description").getAsString();
            return new SQLSkill(name, tables, database, description);
        }

        Map<String, Object> paramsMap = new Gson().fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        return new Skill(name, type, paramsMap);
    }

    public Skill create(String name, String type, Map<String, Object> params) {
        JsonObject jsonParams = new Gson().toJsonTree(params).getAsJsonObject();
        return create(name, type, jsonParams);
    }

    public Skill update(String name, Skill updateSkill) {
        JsonObject params = new JsonObject();
        for (Map.Entry<String, Object> entry : updateSkill.params.entrySet()) {
            params.add(entry.getKey(), new Gson().toJsonTree(entry.getValue()));
        }
        api.updateSkill(this.project.getName(), name, updateSkill.name, updateSkill.type, params);
        return updateSkill;
    }

    public void drop(String name) {
        api.deleteSkill(this.project.getName(), name);
    }
}
