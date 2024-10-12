package mindsdb.models.skill;

import java.util.List;

public class SQLSkill extends Skill {
    public SQLSkill(String name, List<String> tables, String database, String description) {
        super(name, "sql", createParams(database, tables, description));
    }

}