package mindsdb.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Model;
import mindsdb.models.ModelVersion;
import mindsdb.models.Project;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ModelsTest {
    private RestAPI api;
    private Project project;
    private Models models;

    @BeforeEach
    public void setUp() {
        api = mock(RestAPI.class);
        project = mock(Project.class);
        models = new Models(project, api);
    }

    @Test
    public void testListModels() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] { "model1", "model2" }),
                        StringColumn.create("version", new String[] { "1", "2" }));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        List<Model> modelList = models.listModels(null);

        assertNotNull(modelList);
        assertEquals(2, modelList.size());
        assertEquals("model1", modelList.get(0).name);
        assertEquals("model2", modelList.get(1).name);
    }

    @Test
    public void testListModelsWithName() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] { "model1" }),
                        StringColumn.create("version", new String[] { "1" }));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        List<Model> modelList = models.listModels("model1");

        assertNotNull(modelList);
        assertEquals(1, modelList.size());
        assertEquals("model1", modelList.get(0).name);
    }

    @Test
    public void testListModelsWithVersion() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] { "model1" }),
                        StringColumn.create("version", new String[] { "1" }));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        List<ModelVersion> modelVersionList = models.listModelsWithVersion("model1", 1);

        assertNotNull(modelVersionList);
        assertEquals(1, modelVersionList.size());
        assertEquals("model1", modelVersionList.get(0).name);
        assertEquals(1, modelVersionList.get(0).version);
    }

    @Test
    public void testGetModel() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] { "model1" }),
                        StringColumn.create("version", new String[] { "1" }));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        Model model = models.getModel("model1");

        assertNotNull(model);
        assertEquals("model1", model.name);
    }

    @Test
    public void testGetModelWithVersion() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] { "model1" }),
                        StringColumn.create("version", new String[] { "1" }));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        ModelVersion modelVersion = models.getModelWithVersion("model1", 1);

        assertNotNull(modelVersion);
        assertEquals("model1", modelVersion.name);
        assertEquals(1, modelVersion.version);
    }

    @Test
    public void testGetNonExistentModel() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] {}),
                        StringColumn.create("version", new String[] {}));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> models.getModel("nonexistent_model"));
        assertNotNull(exception);
    }

    @Test
    public void testGetNonExistentModelWithVersion() {
        Table mockResponse = Table.create("models")
                .addColumns(
                        StringColumn.create("name", new String[] {}),
                        StringColumn.create("version", new String[] {}));

        Query mockQuery = mock(Query.class);
        when(mockQuery.fetch()).thenReturn(mockResponse);
        when(project.query(anyString())).thenReturn(mockQuery);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> models.getModelWithVersion("nonexistent_model", 1));
        assertNotNull(exception);
    }
}