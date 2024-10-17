package mindsdb.models.Agent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mindsdb.models.skill.Skill;
import mindsdb.services.Agents;

public class Agent {

    public String name;
    public String modelName;
    public List<Skill> skills;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String provider;
    public JsonObject params;
    public Agents agents;

    public Agent(String name, String modelName, List<Skill> skills, JsonObject params, LocalDateTime createdAt,
            LocalDateTime updatedAt, String provider, Agents agents) {
        this.name = name;
        this.modelName = modelName;
        this.skills = skills;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.provider = provider;
        this.params = params;
        this.agents = agents;
    }

    public Agent(String name, String modelName, List<Skill> skills, JsonObject params, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this(name, modelName, skills, params, createdAt, updatedAt, null, null);
    }

    public AgentCompletion completion(List<Map<String, String>> messages) {
        return null;
    }

    public static Agent fromJson(JsonObject data, Agents agents) {
        String name = data.get("name").getAsString();
        String modelName = data.get("model_name").getAsString();

        JsonArray skillsArray = data.getAsJsonArray("skills");
        List<Skill> skills = new ArrayList<>();
        for (JsonElement skillElement : skillsArray) {
            skills.add(Skill.fromJson(skillElement.getAsJsonObject()));
        }

        JsonObject params = data.getAsJsonObject("params");
        LocalDateTime createdAt = LocalDateTime.parse(data.get("created_at").getAsString());
        LocalDateTime updatedAt = LocalDateTime.parse(data.get("updated_at").getAsString());
        String provider = data.get("provider").getAsString();

        return new Agent(name, modelName, skills, params, createdAt, updatedAt, provider, agents);
    }
}
