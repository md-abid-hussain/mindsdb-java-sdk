package mindsdb.models;

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
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

public class MDBTableTest {
    @Mock
    private Database mockDatabase;
    @Mock
    private Project mockProject;
    @Mock
    private RestAPI mockApi;

    private MDBTable mdbTable;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDatabase.getApi()).thenReturn(mockApi);
        when(mockDatabase.getName()).thenReturn("testDatabase");
        when(mockProject.getApi()).thenReturn(mockApi);
        when(mockProject.getName()).thenReturn("testProject");

        mdbTable = new MDBTable(mockDatabase, "testTable");
    }

    @Test
    public void testConstructorWithDatabase() {
        assertEquals("testTable", mdbTable.getName());
        assertEquals("testDatabase.testTable", mdbTable.getTableName());
        assertNotNull(mdbTable.getFilters());
        assertEquals(0, mdbTable.getFilters().size());
        assertEquals(null, mdbTable.getLimit());
        assertEquals(null, mdbTable.getTrackColumn());
    }

    @Test
    public void testConstructorWithProject() {
        MDBTable tableFromProject = new MDBTable(mockProject, "testTable");
        assertEquals("testTable", tableFromProject.getName());
        assertEquals("testProject.testTable", tableFromProject.getTableName());
    }

    @Test
    public void testFilter() {
        MDBTable filteredTable = mdbTable.filter("column1=value1", "column2=value2");
        assertEquals(2, filteredTable.getFilters().size());
        assertEquals("value1", filteredTable.getFilters().get("column1"));
        assertEquals("value2", filteredTable.getFilters().get("column2"));
    }

    @Test
    public void testLimit() {
        MDBTable limitedTable = mdbTable.limit(10);
        assertEquals(10, limitedTable.getLimit());
    }

    @Test
    public void testTrack() {
        MDBTable trackedTable = mdbTable.track("column1");
        assertEquals("column1", trackedTable.getTrackColumn());
    }

    @Test
    public void testInsertQuery() {
        Query mockQuery = mock(Query.class);
        when(mockQuery.getSql()).thenReturn("SELECT * FROM testQuery");
        when(mockQuery.getDatabase()).thenReturn(null);

        mdbTable.insert(mockQuery);
        verify(mockApi, times(1)).sqlQuery("INSERT INTO testDatabase.testTable (SELECT * FROM testQuery)");
    }

    @Test
    public void testInsertTable() {
        Table mockTable = mock(Table.class);
        when(mockTable.columnNames()).thenReturn(List.of("column1", "column2"));
        when(mockTable.rowCount()).thenReturn(1);
        Row mockRow = mock(Row.class);
        when(mockTable.row(0)).thenReturn(mockRow);
        when(mockRow.getObject(0)).thenReturn("value1");
        when(mockRow.getObject(1)).thenReturn(2);

        mdbTable.insert(mockTable);
        verify(mockApi, times(1))
                .sqlQuery("INSERT INTO testDatabase.testTable (column1, column2) VALUES ('value1', 2)");
    }

    @Test
    public void testDelete() {
        mdbTable.delete("column1=value1", "column2=value2");
        verify(mockApi, times(1))
                .sqlQuery("DELETE FROM testDatabase.testTable WHERE column1=value1 AND column2=value2;");
    }

    @Test
    public void testUpdate() {
        Map<String, Object> values = new HashMap<>();
        values.put("column1", "newValue");
        values.put("column2", 2);

        mdbTable.update(values, "column3=value3");
        verify(mockApi, times(1))
                .sqlQuery("UPDATE testDatabase.testTable SET column1 = 'newValue', column2 = 2 WHERE column3=value3");
    }

    @Test
    public void testToString() {
        MDBTable table = mdbTable.filter("column1=value1").limit(10).track("column2");
        String expectedString = "MDBTable(testDatabase.testTable, column1 = value1, limit=10)";
        assertEquals(expectedString, table.toString());
    }
}