package mindsdb.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ModelTest {
    @Mock
    private Project mockProject;
    @Mock
    private RestAPI mockApi;

    private Model model;
    private Map<String, Object> data;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockProject.getApi()).thenReturn(mockApi);
        when(mockProject.getName()).thenReturn("testProject");

        data = new HashMap<>();
        data.put("name", "testModel");
        data.put("version", 1);
        data.put("status", "training");

        model = new Model(mockProject, data);
    }

    @Test
    public void testConstructor() {
        assertEquals("testModel", model.getName());
        assertEquals(1, model.getVersion());
        assertEquals("testProject.testModel.1", model.getIdentifier());
    }

    @Test
    public void testToString() {
        String expectedString = "Model(testModel, version=1, status=training)";
        assertEquals(expectedString, model.toString());
    }

    @Test
    public void testPredictWithTableAndParams() {
        Table mockTable = mock(Table.class);
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");

        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "prediction" }));

        when(mockApi.modelPredict(anyString(), anyString(), any(Table.class), any(Map.class), anyInt()))
                .thenReturn(mockResponse);

        Table result = model.predict(mockTable, params);

        assertNotNull(result);
        assertEquals("response", result.name());
        assertEquals("prediction", result.stringColumn("result").get(0));
    }

    @Test
    public void testPredictWithTable() {
        Table mockTable = mock(Table.class);

        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "prediction" }));

        when(mockApi.modelPredict(anyString(), anyString(), any(Table.class), isNull(), anyInt()))
                .thenReturn(mockResponse);

        Table result = model.predict(mockTable);

        assertNotNull(result);
        assertEquals("response", result.name());
        assertEquals("prediction", result.stringColumn("result").get(0));
    }

    @Test
    public void testPredictWithMapAndParams() {
        Map<String, String> inputData = new HashMap<>();
        inputData.put("input1", "value1");
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");

        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "prediction" }));

        when(mockApi.modelPredict(anyString(), anyString(), any(Map.class), any(Map.class), anyInt()))
                .thenReturn(mockResponse);

        Table result = model.predict(inputData, params);

        assertNotNull(result);
        assertEquals("response", result.name());
        assertEquals("prediction", result.stringColumn("result").get(0));
    }

    @Test
    public void testPredictWithMap() {
        Map<String, String> inputData = new HashMap<>();
        inputData.put("input1", "value1");

        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "prediction" }));

        when(mockApi.modelPredict(anyString(), anyString(), any(Map.class), isNull(), anyInt()))
                .thenReturn(mockResponse);

        Table result = model.predict(inputData);

        assertNotNull(result);
        assertEquals("response", result.name());
        assertEquals("prediction", result.stringColumn("result").get(0));
    }

    // @Test
    // public void testWaitComplete() throws InterruptedException {
    // when(mockApi.modelPredict(anyString(), anyString(), any(Map.class), isNull(),
    // anyInt()))
    // .thenReturn(Table.create("response")
    // .addColumns(StringColumn.create("status", new String[] { "training",
    // "complete" })));

    // doAnswer(invocation -> {
    // model.getData().put("status", "complete");
    // return null;
    // }).when(mockProject).getModel(anyString());

    // model.waitComplete();

    // assertEquals("complete", model.getStatus());
    // }

    @Test
    public void testGetStatus() {
        when(mockProject.getModel(anyString())).thenReturn(model);

        String status = model.getStatus();

        assertNotNull(status);
        assertEquals("training", status);
    }

    @Test
    public void testDescribe() {
        Table mockResponse = Table.create("description")
                .addColumns(StringColumn.create("info", new String[] { "description" }));

        when(mockApi.sqlQuery(anyString())).thenReturn(mockResponse);

        Table result = model.describe("type");

        assertNotNull(result);
        assertEquals("description", result.name());
        assertEquals("description", result.stringColumn("info").get(0));
    }

    @Test
    public void testDescribeWithoutType() {
        Table mockResponse = Table.create("description")
                .addColumns(StringColumn.create("info", new String[] { "description" }));

        when(mockApi.sqlQuery(anyString())).thenReturn(mockResponse);

        Table result = model.describe();

        assertNotNull(result);
        assertEquals("description", result.name());
        assertEquals("description", result.stringColumn("info").get(0));
    }

    @Test
    public void testListVersions() {
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("name", "testModel");
        versionData.put("version", 1);
        List<ModelVersion> mockVersions = List.of(new ModelVersion(mockProject, versionData));

        when(mockProject.listModelsWithVersion(anyString(), isNull())).thenReturn(mockVersions);

        List<ModelVersion> versions = model.listVersions();

        assertNotNull(versions);
        assertEquals(1, versions.size());
        assertEquals("testModel", versions.get(0).getName());
        assertEquals(1, versions.get(0).getVersion());
    }

    @Test
    public void testGetVersion() {
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("name", "testModel");
        versionData.put("version", 1);
        List<ModelVersion> mockVersions = List.of(new ModelVersion(mockProject, versionData));

        when(mockProject.listModelsWithVersion(anyString(), anyInt())).thenReturn(mockVersions);

        ModelVersion version = model.getVersion(1);

        assertNotNull(version);
        assertEquals("testModel", version.getName());
        assertEquals(1, version.getVersion());
    }

    @Test
    public void testDropVersion() {
        model.dropVersion(1);

        verify(mockProject, times(1)).dropModelVersion("testModel", 1);
    }

    @Test
    public void testSetActive() {
        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "active" }));

        when(mockApi.sqlQuery(anyString())).thenReturn(mockResponse);

        model.setActive(1);

        verify(mockApi, times(1)).sqlQuery("SET active = testModel.1");

    }

    @Test
    public void testRetrain() {
        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "retrained" }));

        when(mockApi.sqlQuery(anyString())).thenReturn(mockResponse);

        Model retrainedModel = model.retrain("SELECT * FROM data", "testDatabase", null, null);

        assertNotNull(retrainedModel);
    }

    @Test
    public void testFinetune() {
        Table mockResponse = Table.create("response")
                .addColumns(StringColumn.create("result", new String[] { "finetuned" }));

        when(mockApi.sqlQuery(anyString())).thenReturn(mockResponse);

        Model finetunedModel = model.finetune("SELECT * FROM data", "testDatabase", null, null);
        finetunedModel.getData().put("status", "finetuned");

        assertNotNull(finetunedModel);
        assertEquals("finetuned", finetunedModel.getStatus());
    }
}