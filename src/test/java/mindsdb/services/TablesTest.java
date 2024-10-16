package mindsdb.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Table;
import tech.tablesaw.api.StringColumn;

class TablesTest {

    private Tables tables;
    private Database database;
    private RestAPI api;

    @BeforeEach
    void setUp() {
        api = mock(RestAPI.class);
        database = mock(Database.class);
        when(database.query(anyString())).thenReturn(mock(Query.class));
        database.name = "files";
        tables = new Tables(database, api);
    }

    @Test
    void list() {
        List<String> tableNames = List.of("table1", "table2");
        tech.tablesaw.api.Table response = tech.tablesaw.api.Table.create("response");
        response.addColumns(StringColumn.create("name", tableNames));
        when(database.query("SHOW TABLES").fetch()).thenReturn(response);

        List<Table> result = tables.list();

        assertEquals(2, result.size());
        assertEquals("table1", result.get(0).name);
        assertEquals("table2", result.get(1).name);
    }

    @Test
    void get() {
        String tableName = "table1";
        Table result = tables.get(tableName);

        assertNotNull(result);
        assertEquals(tableName, result.name);
    }

    @Test
    void create() {
        String tableName = "new_table";
        Query query = new Query(api, "SELECT * FROM old_table", "database");
        when(api.sqlQuery(anyString())).thenReturn(mock(tech.tablesaw.api.Table.class));

        Table result = tables.create(tableName, query, true);

        assertNotNull(result);
        assertEquals(tableName, result.name);
        verify(api, times(1)).sqlQuery(anyString());
    }

    @Test
    void testCreate() {
        String tableName = "new_table";
        tech.tablesaw.api.Table df = tech.tablesaw.api.Table.create("new_table");
        Table mockTable = mock(Table.class);
        doNothing().when(api).uploadFile(anyString(), any(tech.tablesaw.api.Table.class));
        mockTable.name = tableName;
        Table result = tables.create(tableName, df, true);

        assertNotNull(result);
        assertEquals(mockTable.name, result.name);
        verify(api, times(1)).uploadFile(anyString(),
                any(tech.tablesaw.api.Table.class));
    }

    @Test
    void drop() {
        String tableName = "table_to_drop";
        // Mock the sqlQuery method to return a specific value
        when(api.sqlQuery(anyString())).thenReturn(null);

        tables.drop(tableName);

        verify(api, times(1)).sqlQuery(anyString());
    }
}