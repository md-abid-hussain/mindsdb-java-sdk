package mindsdb.models.skill;

public class RetrievalSkill extends Skill {

    public RetrievalSkill(String name, String knoweldgeBase, String description) {
        super(name, "retrieval", createParams(knoweldgeBase, null, description));
    }
}