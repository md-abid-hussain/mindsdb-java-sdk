package mindsdb.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import kong.unirest.core.UnirestException;
import mindsdb.connectors.RestAPI;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class QueryTest {

    @Mock
    private RestAPI api;

    private Query query;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        query = new Query(api, "SELECT * FROM table", "test_database");
    }

    @Test
    public void testFetch() {
        // Mock the response from the API
        StringColumn column = StringColumn.create("column", new String[] { "value1", "value2" });
        Table mockTable = Table.create(column);

        when(api.sqlQuery("SELECT * FROM table", "test_database")).thenReturn(mockTable);

        // Call the method
        Table result = query.fetch();

        // Verify the results
        assertNotNull(result);
        assertEquals(2, result.rowCount());
        assertEquals("value1", result.stringColumn("column").get(0));
        assertEquals("value2", result.stringColumn("column").get(1));
    }

    @Test
    public void testFetchWithException() {
        // Mock the API to throw an exception
        when(api.sqlQuery("SELECT * FROM table", "test_database")).thenThrow(new UnirestException("API error"));

        // Call the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            query.fetch();
        });

        // Verify the exception message
        assertEquals("Error executing SQL query: API error", exception.getMessage());
    }

    @Test
    public void testToString() {
        // Test the toString method
        String result = query.toString();
        assertEquals("Query(SELECT * FROM table)", result);

        // Test with a longer SQL string
        query.setSql("SELECT * FROM table WHERE column1 = 'value1' AND column2 = 'value2'");
        result = query.toString();
        assertEquals("Query(SELECT * FROM table WHERE column1 = '...)", result);
    }
}