package mindsdb.models.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class RetrievalSkillTest {

    private RetrievalSkill retrievalSkill;

    @BeforeEach
    public void setUp() {
        retrievalSkill = new RetrievalSkill("test_retrieval_skill", "test_knowledge_base", "test_description");
    }

    @Test
    public void testRetrievalSkillCreation() {
        assertNotNull(retrievalSkill);
        assertEquals("test_retrieval_skill", retrievalSkill.getName());
        assertEquals("retrieval", retrievalSkill.getType());
        assertEquals(2, retrievalSkill.getParams().size());
        assertEquals("test_knowledge_base", retrievalSkill.getParams().get("database"));
        assertEquals("test_description", retrievalSkill.getParams().get("description"));
        assertNull(retrievalSkill.getParams().get("tables"));
    }

    @Test
    public void testDescribe() {
        String description = retrievalSkill.describe();
        assertNotNull(description);
        assertEquals(
                "Skill(name=test_retrieval_skill, type=retrieval, params={database=test_knowledge_base, description=test_description})",
                description);
    }

    @Test
    public void testFromJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "json_retrieval_skill");
        json.addProperty("type", "retrieval");
        JsonObject paramsJson = new JsonObject();
        paramsJson.addProperty("database", "json_knowledge_base");
        paramsJson.addProperty("description", "json_description");
        paramsJson.add("tables", null);
        json.add("params", paramsJson);

        Skill jsonSkill = Skill.fromJson(json);
        assertNotNull(jsonSkill);
        assertEquals("json_retrieval_skill", jsonSkill.getName());
        assertEquals("retrieval", jsonSkill.getType());
        assertEquals(3, jsonSkill.getParams().size());
        assertEquals("json_knowledge_base", jsonSkill.getParams().get("database"));
        assertEquals("json_description", jsonSkill.getParams().get("description"));
        assertNull(jsonSkill.getParams().get("tables"));
    }

    @Test
    public void testToString() {
        String toString = retrievalSkill.toString();
        assertEquals("Skill(name=test_retrieval_skill)", toString);
    }
}