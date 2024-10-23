package mindsdb.models.skill;

import java.util.List;

/**
 * The SQLSkill class represents a specific type of skill that focuses on SQL
 * tasks.
 * It extends the Skill class and is initialized with a name, list of tables,
 * database, and description.
 * 
 * @param name        The name of the SQL skill.
 * @param tables      The list of tables associated with the SQL skill.
 * @param database    The database associated with the SQL skill.
 * @param description A brief description of the SQL skill.
 * 
 */
public class SQLSkill extends Skill {
    public SQLSkill(String name, List<String> tables, String database, String description) {
        super(name, "sql", createParams(database, tables, description));
    }

}