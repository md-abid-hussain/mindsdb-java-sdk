package mindsdb.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import tech.tablesaw.api.Table;

public class KnowledgeBaseTest {

    @Mock
    private RestAPI api;

    @Mock
    private Project project;

    private KnowledgeBase knowledgeBase;
    private Map<String, Object> data;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        data = new HashMap<>();
        data.put("name", "testKnowledgeBase");
        data.put("params",
                "{\"metadata_columns\": [\"col1\", \"col2\"], \"content_columns\": [\"col3\", \"col4\"], \"id_column\": \"id\"}");
        data.put("database", "testDatabase");
        when(project.getName()).thenReturn("testProject");
        knowledgeBase = new KnowledgeBase(api, project, data);
    }

    @Test
    public void testConstructor() {
        assertEquals("testKnowledgeBase", knowledgeBase.getName());
        assertEquals("testProject.testKnowledgeBase", knowledgeBase.getTableName());
        assertNotNull(knowledgeBase.getParams());
        assertEquals(Arrays.asList("col1", "col2"), knowledgeBase.getMetadataColumns());
        assertEquals(Arrays.asList("col3", "col4"), knowledgeBase.getContentColumns());
        assertEquals("id", knowledgeBase.getIdColumn());
    }

    @Test
    public void testFind() {
        KnowledgeBase foundKnowledgeBase = knowledgeBase.find("testQuery", 10);
        assertNotNull(foundKnowledgeBase);
        assertEquals("testQuery", foundKnowledgeBase.getQuery());
        assertEquals(10, foundKnowledgeBase.getLimit());
    }

    @Test
    public void testInsertFiles() {
        List<String> filePaths = Arrays.asList("file1.txt", "file2.txt");
        knowledgeBase.insertFiles(filePaths);
        verify(api, times(1)).insertFilesIntoKnowledgeBase("testProject", "testKnowledgeBase", filePaths);
    }

    @Test
    public void testInsertWebpages() {
        List<String> urls = Arrays.asList("http://example.com", "http://example.org");
        int crawlDepth = 2;
        List<String> filters = Arrays.asList("filter1", "filter2");
        knowledgeBase.insertWebpages(urls, crawlDepth, filters);
        verify(api, times(1)).insertWebpagesIntoKnowledgeBase("testProject", "testKnowledgeBase", urls, crawlDepth,
                filters);
    }

    @Test
    public void testInsertTable() {
        Table table = Table.create("data");
        table.addColumns(
                tech.tablesaw.api.StringColumn.create("col1", new String[] { "val1", "val2" }),
                tech.tablesaw.api.StringColumn.create("col2", new String[] { "val3", "val4" }));

        knowledgeBase.insert(table);
        String expectedQuery = "INSERT INTO testProject.testKnowledgeBase (col1, col2) VALUES ('val1', 'val3'), ('val2', 'val4');";
        verify(api, times(1)).sqlQuery(expectedQuery, "testProject");
    }

    @Test
    public void testInsertQuery() {
        Query query = mock(Query.class);
        when(query.getDatabase()).thenReturn("testDatabase");
        when(query.getSql()).thenReturn("SELECT * FROM testTable");

        knowledgeBase.insert(query);
        String expectedQuery = "INSERT INTO testProject.testKnowledgeBase SELECT * FROM testDatabase (SELECT * FROM testTable) ;";
        verify(api, times(1)).sqlQuery(expectedQuery, "testDatabase");
    }

    @Test
    public void testInsertMap() {
        Map<String, String> insertData = new HashMap<>();
        insertData.put("col1", "val1");
        insertData.put("col2", "val2");

        knowledgeBase.insert(insertData);
        String expectedQuery = "INSERT INTO testProject.testKnowledgeBase (col2, col1) VALUES ('val2', 'val1');";
        verify(api, times(1)).sqlQuery(expectedQuery, "testProject");
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        KnowledgeBase clonedKnowledgeBase = knowledgeBase.clone();
        assertNotNull(clonedKnowledgeBase);
        assertEquals(knowledgeBase.getName(), clonedKnowledgeBase.getName());
        assertEquals(knowledgeBase.getTableName(), clonedKnowledgeBase.getTableName());
        assertEquals(knowledgeBase.getParams(), clonedKnowledgeBase.getParams());
        assertEquals(knowledgeBase.getMetadataColumns(), clonedKnowledgeBase.getMetadataColumns());
        assertEquals(knowledgeBase.getContentColumns(), clonedKnowledgeBase.getContentColumns());
        assertEquals(knowledgeBase.getIdColumn(), clonedKnowledgeBase.getIdColumn());
    }
}