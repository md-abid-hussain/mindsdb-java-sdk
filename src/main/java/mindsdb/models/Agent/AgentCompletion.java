package mindsdb.models.Agent;

import java.util.List;

/**
 * Represents a full MindsDB agent completion response.
 *
 * Attributes:
 * content: The completion content.
 * context: Only relevant for retrieval agents. Contains the context retrieved
 * from the knowledge base.
 */
public class AgentCompletion {
    private final String content;
    private final List<String> context;

    public AgentCompletion(String content, List<String> context) {
        this.content = content;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("%s(content: %s, context: %s)", this.getClass().getSimpleName(), content, context);
    }

}
