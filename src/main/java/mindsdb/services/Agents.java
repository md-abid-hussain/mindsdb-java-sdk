package mindsdb.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Agent.Agent;
import mindsdb.models.Agent.AgentCompletion;
import mindsdb.models.Project;

/**
 * Agents service
 */
public class Agents {

    private final Project project;
    private final RestAPI api;
    private final KnowledgeBases knowledgeBases;
    private final Models models;
    private final Skills skills;
    private final Databases databases;
    private final MLEngines mlEngines;

    public Agents(Project project, RestAPI api) {
        this.project = project;
        this.api = api;

        this.knowledgeBases = project.knowledgeBases;
        this.models = project.models;
        this.skills = project.skills;

        this.databases = project.server.databases;
        this.mlEngines = project.server.mlEngines;
    }

    /**
     * List all agents
     * 
     * @return List of agents
     */
    public List<Agent> list() {
        JsonArray jsonAgents = api.agents(project.getName());
        System.out.println(jsonAgents);
        List<Agent> agents = new ArrayList<>();
        for (JsonElement jsonAgent : jsonAgents) {
            JsonObject agentData = jsonAgent.getAsJsonObject();
            Agent agent = Agent.fromJson(agentData, this);
            agents.add(agent);
        }

        return agents;
    }

    public Agent get(String name) {
        JsonObject jsonAgent = api.agent(project.getName(), name);
        return Agent.fromJson(jsonAgent, this);
    }

    public AgentCompletion completion(String name, List<JsonObject> messages) {
        JsonObject data = api.agentCompletion(this.project.getName(), name, messages);

        JsonObject message = data.getAsJsonObject("message");

        String content = message.get("content").getAsString();
        if (message.has("context")) {
            List<String> context = new ArrayList<>();
            message.getAsJsonArray("context").forEach(jsonElement -> context.add(jsonElement.getAsString()));
            return new AgentCompletion(content, context);
        }

        return new AgentCompletion(content, null);

    }

    // private KnowledgeBase createDefaultKnowledgeBase(Agent agent, String name) {
    // try {
    // this.mlEngines.get("langchain_embedding");
    // } catch (Exception e) {
    // this.mlEngines.create("langchain_embedding", "langchain_embedding");
    // }

    // // Include API keys in embeddings.
    // KnowledgeBase kb;
    // if ("mindsdb".equals(agent.provider)) {
    // Model agentModel = this.models.getModel(agent.modelName);
    // JsonObject trainingOptions = agentModel.data.containsKey("training_options")
    // ? new JsonObject(agentModel.data.get("training_options"))
    // : new JsonObject();
    // JsonObject trainingOptionsUsing = trainingOptions.has("using")
    // ? trainingOptions.getAsJsonObject("using")
    // : new JsonObject();

    // Map<String, Object> apiKeyParams = new HashMap<>();
    // for (Map.Entry<String, JsonElement> entry : trainingOptionsUsing.entrySet())
    // {
    // if (entry.getKey().contains("api_key")) {
    // apiKeyParams.put(entry.getKey(), entry.getValue().getAsString());
    // }
    // }
    // kb = api.createKnowledgeBase(name, apiKeyParams);
    // } else {
    // kb = api.createKnowledgeBase(name);
    // }

    // // Wait for underlying embedding model to finish training.
    // kb.getModel().waitComplete();
    // return kb;
    // }

}
