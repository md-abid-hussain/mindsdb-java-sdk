package mindsdb.models;

import java.util.Map;

/**
 * ModelVersion class for handling model version.
 */
public class ModelVersion extends Model {

    /**
     * Constructor for ModelVersion
     * @param project Project object
     * @param data   Data for the model version
     */
    public ModelVersion(Project project, Map<String, Object> data) {
        super(project, data);
    }

}
