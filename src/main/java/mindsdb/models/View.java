package mindsdb.models;

public class View extends Table {
    public Project project;

    public View(Project project, String name) {
        super(project, name);
        this.project = project;
    }

}
