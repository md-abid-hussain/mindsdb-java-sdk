package mindsdb.models;

import lombok.Getter;

/**
 * The View class represents a specific type of table that focuses on views.
 */
@Getter
public class View extends MDBTable {
    private final Project project;

    /**
     * Constructs a new View instance with the specified project and name.
     * @param project   The project associated with the view.
     * @param name  The name of the view.
     */
    public View(Project project, String name) {
        super(project, name);
        this.project = project;
    }

}
