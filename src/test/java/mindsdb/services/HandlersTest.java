package mindsdb.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Handler;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class HandlersTest {

    @Mock
    private RestAPI api;

    private Handlers handlers;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handlers = new Handlers(api, "test_type");
    }

    @Test
    public void testList() {
        // Mock the response from the API
        StringColumn nameColumn = StringColumn.create("name", new String[] { "handler1", "handler2" });
        StringColumn typeColumn = StringColumn.create("type", new String[] { "test_type", "test_type" });
        Table mockTable = Table.create(nameColumn, typeColumn);

        when(api.sqlQuery(anyString())).thenReturn(mockTable);

        // Call the method
        List<Handler> handlerList = handlers.list();

        // Verify the results
        assertEquals(2, handlerList.size());
        assertEquals("handler1", handlerList.get(0).getName());
        assertEquals("handler2", handlerList.get(1).getName());
    }

    @Test
    public void testGet() {
        // Mock the response from the API
        StringColumn nameColumn = StringColumn.create("name", new String[] { "handler1", "handler2" });
        StringColumn typeColumn = StringColumn.create("type", new String[] { "test_type", "test_type" });
        Table mockTable = Table.create(nameColumn, typeColumn);

        when(api.sqlQuery(anyString())).thenReturn(mockTable);

        // Call the method
        Handler handler = handlers.get("handler1");

        // Verify the results
        assertNotNull(handler);
        assertEquals("handler1", handler.getName());
    }

    @Test
    public void testGetHandlerNotFound() {
        // Mock the response from the API
        StringColumn nameColumn = StringColumn.create("name", new String[] { "handler1", "handler2" });
        StringColumn typeColumn = StringColumn.create("type", new String[] { "test_type", "test_type" });
        Table mockTable = Table.create(nameColumn, typeColumn);

        when(api.sqlQuery(anyString())).thenReturn(mockTable);

        // Call the method and expect an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            handlers.get("handler3");
        });

        // Verify the exception message
        assertEquals("Handler doesn't exist", exception.getMessage());
    }
}