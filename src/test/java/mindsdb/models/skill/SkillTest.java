package mindsdb.models.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SkillTest {

    private Skill skill;

    @BeforeEach
    public void setUp() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        skill = new Skill("test_skill", "sql", params);
    }

    @Test
    public void testSkillCreation() {
        assertNotNull(skill);
        assertEquals("test_skill", skill.getName());
        assertEquals("sql", skill.getType());
        assertEquals(1, skill.getParams().size());
        assertEquals("value", skill.getParams().get("key"));
    }

    @Test
    public void testDescribe() {
        String description = skill.describe();
        assertNotNull(description);
        assertEquals("Skill(name=test_skill, type=sql, params={key=value})", description);
    }

    @Test
    public void testFromJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "json_skill");
        json.addProperty("type", "sql");
        JsonObject paramsJson = new JsonObject();
        paramsJson.add("key", new JsonPrimitive("value"));
        json.add("params", paramsJson);

        Skill jsonSkill = Skill.fromJson(json);
        assertNotNull(jsonSkill);
        assertEquals("json_skill", jsonSkill.getName());
        assertEquals("sql", jsonSkill.getType());
        assertEquals(1, jsonSkill.getParams().size());
        assertEquals("value", jsonSkill.getParams().get("key"));
    }

    @Test
    public void testCreateParams() {
        List<String> tables = List.of("table1", "table2");
        Map<String, Object> params = Skill.createParams("test_database", tables, "test_description");

        assertNotNull(params);
        assertEquals("test_database", params.get("database"));
        assertEquals("test_description", params.get("description"));
        assertTrue(params.get("tables") instanceof List);
        List<?> tablesList = (List<?>) params.get("tables");
        assertEquals(2, tablesList.size());
        assertEquals("table1", tablesList.get(0));
        assertEquals("table2", tablesList.get(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        Skill skill1 = new Skill("test_skill", "sql", params);
        Skill skill2 = new Skill("test_skill", "sql", params);

        assertEquals(skill1, skill2);
        assertEquals(skill1.hashCode(), skill2.hashCode());

        Skill skill3 = new Skill("test_skill", "sql", null);
        assertNotEquals(skill1, skill3);
        assertNotEquals(skill1.hashCode(), skill3.hashCode());
    }

    @Test
    public void testToString() {
        String toString = skill.toString();
        assertEquals("Skill(name=test_skill)", toString);
    }
}