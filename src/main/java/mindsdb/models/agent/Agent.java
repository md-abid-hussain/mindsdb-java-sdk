package mindsdb.models.agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;
import mindsdb.models.skill.Skill;
import mindsdb.services.Agents;

/**
 * Represents a MindsDB agent.
 *
 * Working with agents:
 *
 * Get an agent by name:
 *
 * <pre>
 * {@code
 * Agent agent = agents.get("my_agent");
 * }
 * </pre>
 *
 * Query an agent:
 *
 * <pre>
 * {@code
 * AgentCompletion completion = agent.completion(List.of(Map.of("question", "What is your name?", "answer", null)));
 * System.out.println(completion.getContent());
 * }
 * </pre>
 *
 * Query an agent with streaming:
 *
 * <pre>
 * {@code
 * AgentCompletion completion = agent
 *         .completionStream(List.of(Map.of("question", "What is your name?", "answer", null)));
 * for (CompletionChunk chunk : completion) {
 *     System.out.println(chunk.getChoices().get(0).getDelta().getContent());
 * }
 * }
 * </pre>
 *
 * List all agents:
 *
 * <pre>
 * {@code
 * List<Agent> agents = agents.list();
 * }
 * </pre>
 *
 * Create a new agent:
 *
 * <pre>
 * {@code
 * Model model = models.get("my_model"); // Or use models.create(...)
 * // Connect your agent to a MindsDB table.
 * Skill textToSqlSkill = skills.create("text_to_sql", "sql",
 *         Map.of("tables", List.of("my_table"), "database", "my_database"));
 * Agent agent = agents.create("my_agent", model, List.of(textToSqlSkill));
 * }
 * </pre>
 *
 * Update an agent:
 *
 * <pre>
 * {@code
 * Model newModel = models.get("new_model");
 * agent.setModelName(newModel.getName());
 * Skill newSkill = skills.create("new_skill", "sql",
 *         Map.of("tables", List.of("new_table"), "database", "new_database"));
 * agent.getSkills().add(newSkill);
 * Agent updatedAgent = agents.update("my_agent", agent);
 * }
 * </pre>
 *
 * Delete an agent by name:
 *
 * <pre>
 * {@code
 * agents.drop("my_agent");
 * }
 * </pre>
 */
@Getter
@Setter
public class Agent {

    private String name;
    private String modelName;
    private List<Skill> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String provider;
    private JsonObject params;
    private Agents agents;

    /**
     * Constructs a new Agent instance.
     *
     * @param name      - the name of the agent
     * @param modelName - the name of the model associated with the agent
     * @param skills    - the list of skills the agent possesses
     * @param params    - additional parameters for the agent in JSON format
     * @param createdAt - the timestamp when the agent was created
     * @param updatedAt - the timestamp when the agent was last updated
     * @param provider  - the provider of the agent
     * @param agents    - the Agents instance associated with this agent
     */
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

    @Override
    public String toString() {
        return "Agent(name=" + name + ")";
    }

    /**
     * Returns a string representation of the agent.
     * 
     * @return A string representation of the agent.
     */
    public String describe() {
        return "Agent(name=" + name + ", modelName=" + modelName + ", skills=" + skills + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + ", provider=" + provider + ", params=" + params + ")";
    }

    /**
     * Constructor for Agent
     * 
     * @param name      - name of the agent
     * @param modelName - name of the model
     * @param skills    - list of skills
     * @param params    - parameters
     * @param createdAt - created at
     * @param updatedAt - updated at
     */
    public Agent(String name, String modelName, List<Skill> skills, JsonObject params, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this(name, modelName, skills, params, createdAt, updatedAt, null, null);
    }

    /**
     * Converts JSON data to an Agent object.
     *
     * @param data   - the JSON data to convert
     * @param agents - the Agents instance associated with this agent
     * @return An Agent object created from the JSON data.
     */
    public static Agent fromJson(JsonObject data, Agents agents) {
        String name = data.get("name").getAsString();
        String modelName = data.get("model_name").getAsString();

        JsonArray skillsArray = data.getAsJsonArray("skills");
        List<Skill> skills = new ArrayList<>();
        for (JsonElement skillElement : skillsArray) {
            skills.add(Skill.fromJson(skillElement.getAsJsonObject()));
        }

        JsonObject params = data.getAsJsonObject("params");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime createdAt = LocalDateTime.parse(data.get("created_at").getAsString(), formatter);
        LocalDateTime updatedAt = LocalDateTime.parse(data.get("updated_at").getAsString(), formatter);
        String provider = data.get("provider").getAsString();

        return new Agent(name, modelName, skills, params, createdAt, updatedAt, provider, agents);
    }

    /**
     * Generates a completion for the given list of messages.
     *
     * This method takes a list of messages, where each message is represented as a
     * map of key-value pairs.
     * It converts each message into a JSON object and then calls the completion
     * method of the agents with the name and the list of JSON objects.
     *
     * @param messages A list of messages, where each message is a map of key-value
     *                 pairs.
     * @return An AgentCompletion object containing the result of the completion.
     */
    public AgentCompletion completion(List<Map<String, String>> messages) {
        JsonObject messageObject = new JsonObject();
        for (Map<String, String> message : messages) {
            for (Map.Entry<String, String> entry : message.entrySet()) {
                messageObject.addProperty(entry.getKey(), entry.getValue());
            }
        }

        return this.agents.completion(name, List.of(messageObject));
    }

}
