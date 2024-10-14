package mindsdb.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.Handler;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class DatabasesTest {
    private RestAPI api;
    private Databases databases;

    @BeforeEach
    public void setUp() {
        api = mock(RestAPI.class);
        databases = new Databases(api);
    }

    @Test
    public void testListDatabases() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "ENGINE" },
                new String[][] {
                        { "db1", "mysql" },
                        { "db2", "postgres" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        List<Database> databaseList = databases.list();

        assertEquals(2, databaseList.size());
        assertEquals("db1", databaseList.get(0).name);
        assertEquals("mysql", databaseList.get(0).engine);
        assertEquals("db2", databaseList.get(1).name);
        assertEquals("postgres", databaseList.get(1).engine);
    }

    @Test
    public void testGetDatabase() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "ENGINE" },
                new String[][] {
                        { "db1", "mysql" },
                        { "db2", "postgres" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        Database database = databases.get("db1");

        assertNotNull(database);
        assertEquals("db1", database.name);
        assertEquals("mysql", database.engine);
    }

    @Test
    public void testGetNonExistentDatabase() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "ENGINE" },
                new String[][] {
                        { "db1", "mysql" },
                        { "db2", "postgres" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            databases.get("nonexistent");
        });

        String expectedMessage = "Database doesn't exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateDatabase() {
        Map<String, String> connectionArgs = new HashMap<>();
        connectionArgs.put("host", "localhost");
        connectionArgs.put("port", "3306");

        databases.create("new_db", "mysql", connectionArgs);
        StringBuilder astQuery = new StringBuilder(
                "CREATE DATABASE new_db ENGINE = 'mysql'");
        astQuery.append(", PARAMETERS = { ");
        for (Map.Entry<String, String> entry : connectionArgs.entrySet()) {
            astQuery.append("\\\"").append(entry.getKey().replace("\"", "\\\"")).append("\\\"")
                    .append(": ")
                    .append("\\\"").append(entry.getValue().replace("\"", "\\\"")).append("\\\"")
                    .append(", ");
        }
        astQuery.setLength(astQuery.length() - 2);
        astQuery.append(" }");

        astQuery.append(";");
        String expectedQuery = astQuery.toString();
        verify(api, times(1)).sqlQuery(expectedQuery);
    }

    @Test
    public void testCreateDatabaseWithHandler() {
        Handler handler = mock(Handler.class);
        when(handler.getName()).thenReturn("mysql");

        databases.create("new_db", handler.getName(), null);

        String expectedQuery = "CREATE DATABASE new_db ENGINE = 'mysql';";
        verify(api, times(1)).sqlQuery(expectedQuery);
    }

    @Test
    public void testDropDatabase() {
        databases.drop("db_to_drop");

        String expectedQuery = "DROP DATABASE db_to_drop";
        verify(api, times(1)).sqlQuery(expectedQuery);
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