package mindsdb.models.skill;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SQLSkillTest {
    private SQLSkill sqlSkill;
    private List<String> tables;

    @BeforeEach
    public void setUp() {
        tables = List.of("table1", "table2");
        sqlSkill = new SQLSkill("test_sql_skill", tables, "test_database", "test_description");
    }

    @Test
    public void testSQLSkillCreation() {
        assertNotNull(sqlSkill);
        assertEquals("test_sql_skill", sqlSkill.getName());
        assertEquals("sql", sqlSkill.getType());
        Map<String, Object> params = sqlSkill.getParams();
        assertEquals(3, params.size());
        assertEquals("test_database", params.get("database"));
        assertEquals("test_description", params.get("description"));
        assertEquals(tables, params.get("tables"));
    }

    @Test
    public void testDescribe() {
        String description = sqlSkill.describe();
        assertNotNull(description);
        assertEquals(
                "Skill(name=test_sql_skill, type=sql, params={database=test_database, tables=[table1, table2], description=test_description})",
                description);
    }

    @Test
    public void testToString() {
        String toString = sqlSkill.toString();
        assertEquals("Skill(name=test_sql_skill)", toString);
    }
}