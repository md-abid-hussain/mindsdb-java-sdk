package mindsdb.models.skill;

import java.util.List;

/**
 * The SQLSkill class represents a specific type of skill that focuses on SQL
 * tasks.
 * It extends the Skill class and is initialized with a name, list of tables,
 * database, and description.
 */
public class SQLSkill extends Skill {

    /**
     * Constructs a new SQLSkill instance with the specified name, list of tables,
     * @param name      The name of the skill. 
     * @param tables    The list of tables.
     * @param database  The database. 
     * @param description   The description of the skill.
     */
    public SQLSkill(String name, List<String> tables, String database, String description) {
        super(name, "sql", createParams(database, tables, description));
    }

}