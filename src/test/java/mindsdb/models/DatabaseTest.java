package mindsdb.models;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.services.Tables;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class DatabaseTest {

    @Mock
    private RestAPI api;

    @Mock
    private Tables tables;

    private Database database;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        database = new Database(api, "test_db", "mysql");
        database.setTables(tables); // Inject the mocked Tables object
    }

    @Test
    public void testQuery() {
        String sql = "SELECT * FROM test_table";
        Query query = database.query(sql);

        assertNotNull(query);
        assertEquals(sql, query.getSql());
        assertEquals("test_db", query.getDatabase());
    }

    @Test
    public void testListTables() {

        List<mindsdb.models.MDBTable> mockTableList = List.of(
                new mindsdb.models.MDBTable(database, "table1"),
                new mindsdb.models.MDBTable(database, "table2"));
        when(tables.list()).thenReturn(mockTableList);

        List<mindsdb.models.MDBTable> tableList = database.listTables();

        assertNotNull(tableList);
        assertEquals(2, tableList.size());
        assertEquals("table1", tableList.get(0).getName());
        assertEquals("table2", tableList.get(1).getName());
    }

    @Test
    public void testGetTable() {
        mindsdb.models.MDBTable mockTable = mock(mindsdb.models.MDBTable.class);
        when(mockTable.getName()).thenReturn("table1");

        when(tables.get("table1")).thenReturn(mockTable);

        mindsdb.models.MDBTable table = database.getTable("table1");

        assertNotNull(table);
        assertEquals("table1", table.getName());
    }

    @Test
    public void testDropTable() {
        database.dropTable("table1");

        verify(tables, times(1)).drop("table1");
    }

    @Test
    public void testCreateTableWithDataFrame() {
        mindsdb.models.MDBTable mockTable = mock(mindsdb.models.MDBTable.class);
        when(mockTable.getName()).thenReturn("table1");

        when(tables.create(eq("table1"), any(Table.class), eq(true))).thenReturn(mockTable);

        mindsdb.models.MDBTable table = database.createTable("table1", Table.create("table1"), true);

        assertNotNull(table);
        assertEquals("table1", table.getName());
    }

    @Test
    public void testCreateTableWithQuery() {
        Query mockQuery = mock(Query.class);
        mindsdb.models.MDBTable mockTable = mock(mindsdb.models.MDBTable.class);
        when(mockTable.getName()).thenReturn("table1");

        when(tables.create(eq("table1"), eq(mockQuery), eq(true))).thenReturn(mockTable);

        mindsdb.models.MDBTable table = database.createTable("table1", mockQuery, true);

        assertNotNull(table);
        assertEquals("table1", table.getName());
    }

    private Table createMockTable(String[] columnNames, String[][] data) {
        StringColumn[] columns = new StringColumn[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            StringColumn column = StringColumn.create(columnNames[i]);
            for (String[] row : data) {
                column.append(row[i]);
            }
            columns[i] = column;
        }
        return Table.create(columns);
    }
}