package mindsdb.models.agent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class AgentCompletionTest {

    @Test
    public void testAgentCompletionCreation() {
        String content = "This is a test completion";
        List<String> context = List.of("context1", "context2");

        AgentCompletion agentCompletion = new AgentCompletion(content, context);

        assertNotNull(agentCompletion);
        assertEquals(content, agentCompletion.getContent());
        assertEquals(context, agentCompletion.getContext());
    }

    @Test
    public void testToString() {
        String content = "This is a test completion";
        List<String> context = List.of("context1", "context2");

        AgentCompletion agentCompletion = new AgentCompletion(content, context);

        String expectedString = "AgentCompletion(content: This is a test completion, context: [context1, context2])";
        assertEquals(expectedString, agentCompletion.toString());
    }
}