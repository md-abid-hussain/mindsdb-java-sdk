package mindsdb.models.agent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import mindsdb.models.skill.Skill;
import mindsdb.services.Agents;

public class AgentTest {

    private Agent agent;

    @Mock
    private Agents agents;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        JsonObject params = new JsonObject();
        params.addProperty("param1", "value1");

        agent = new Agent(
                "test_agent",
                "test_model",
                List.of(new Skill("test_skill", "sql", Map.of("key", "value"))),
                params,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "test_provider",
                agents);
    }

    @Test
    public void testAgentCreation() {
        assertNotNull(agent);
        assertEquals("test_agent", agent.getName());
        assertEquals("test_model", agent.getModelName());
        assertEquals(1, agent.getSkills().size());
        assertEquals("test_provider", agent.getProvider());
        assertNotNull(agent.getCreatedAt());
        assertNotNull(agent.getUpdatedAt());
        assertEquals(agents, agent.getAgents());
    }

    @Test
    public void testDescribe() {
        String description = agent.describe();
        assertNotNull(description);
        assertEquals(
                "Agent(name=test_agent, modelName=test_model, skills=[Skill(name=test_skill)], createdAt="
                        + agent.getCreatedAt() + ", updatedAt=" + agent.getUpdatedAt()
                        + ", provider=test_provider, params={\"param1\":\"value1\"})",
                description);
    }

    @Test
    public void testCompletion() {
        List<Map<String, String>> messages = List.of(Map.of("question", "What is your name?"));
        AgentCompletion expectedCompletion = new AgentCompletion("This is a test completion",
                List.of("context1", "context2"));

        when(agents.completion(eq("test_agent"), anyList())).thenReturn(expectedCompletion);

        AgentCompletion completion = agent.completion(messages);

        assertNotNull(completion);
        assertEquals("This is a test completion", completion.getContent());
        assertEquals(List.of("context1", "context2"), completion.getContext());
        verify(agents).completion(eq("test_agent"), anyList());
    }

    @Test
    public void testFromJson() {
        JsonObject data = new JsonObject();
        data.addProperty("name", "json_agent");
        data.addProperty("model_name", "json_model");

        JsonArray skillsArray = new JsonArray();
        JsonObject skillObject = new JsonObject();
        skillObject.addProperty("name", "json_skill");
        skillObject.addProperty("type", "sql");
        JsonObject skillParams = new JsonObject();
        skillParams.add("key", new JsonPrimitive("value"));
        skillObject.add("params", skillParams);
        skillsArray.add(skillObject);
        data.add("skills", skillsArray);

        JsonObject params = new JsonObject();
        params.addProperty("param1", "value1");
        data.add("params", params);

        data.addProperty("created_at", "2023-10-23 19:02:22.182000");
        data.addProperty("updated_at", "2023-10-23 19:02:22.182000");
        data.addProperty("provider", "json_provider");

        Agent jsonAgent = Agent.fromJson(data, agents);
        assertNotNull(jsonAgent);
        assertEquals("json_agent", jsonAgent.getName());
        assertEquals("json_model", jsonAgent.getModelName());
        assertEquals("json_provider", jsonAgent.getProvider());
        assertEquals(1, jsonAgent.getSkills().size());
        assertEquals("json_skill", jsonAgent.getSkills().get(0).getName());
        assertEquals("sql", jsonAgent.getSkills().get(0).getType());
        assertEquals("value", jsonAgent.getSkills().get(0).getParams().get("key"));
        assertEquals(params, jsonAgent.getParams());
    }

    @Test
    public void testToString() {
        String toString = agent.toString();
        assertEquals("Agent(name=test_agent)", toString);
    }
}