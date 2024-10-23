package mindsdb.models.skill;

/**
 * The RetrievalSkill class represents a specific type of skill that focuses on
 * retrieval tasks.
 * It extends the Skill class and is initialized with a name, knowledge base,
 * and description.
 */
public class RetrievalSkill extends Skill {

    /**
     * Constructs a new RetrievalSkill instance.
     *
     * @param name          The name of the skill.
     * @param knoweldgeBase The knowledge base associated with the skill.
     * @param description   A description of the skill.
     */
    public RetrievalSkill(String name, String knoweldgeBase, String description) {
        super(name, "retrieval", createParams(knoweldgeBase, null, description));
    }
}