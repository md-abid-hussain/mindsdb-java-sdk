package mindsdb.models;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MLEngineTest {
    private MLEngine mlEngine;
    private String name;
    private String handler;
    private Map<String,String> connectionData;

    @BeforeEach
    public void setUp() {
        name = "testEngine";
        handler = "testHandler";
        connectionData = Map.of(
            "host", "localhost",
            "port", "8080"
        );
        mlEngine = new MLEngine(name, handler, connectionData);
    }

    @Test
    public void testConstructor() {
        assertEquals(name, mlEngine.getName());
        assertEquals(handler, mlEngine.getHandler());
        assertEquals(connectionData, mlEngine.getConnectionData());
    }

    @Test
    public void testGetName() {
        assertEquals(name, mlEngine.getName());
    }

    @Test
    public void testGetHandler() {
        assertEquals(handler, mlEngine.getHandler());
    }

    @Test
    public void testGetConnectionData() {
        assertNotNull(mlEngine.getConnectionData());
        assertEquals("localhost", mlEngine.getConnectionData().get("host"));
        assertEquals("8080", mlEngine.getConnectionData().get("port"));
    }
}