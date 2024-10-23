package mindsdb.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class HandlerTest {

    private JsonObject kwargs;

    @BeforeEach
    public void setUp() {
        kwargs = new JsonObject();
        kwargs.addProperty("name", "testHandler");
        kwargs.addProperty("title", "Test Handler");
        kwargs.addProperty("version", "1.0");
        kwargs.addProperty("description", "This is a test handler.");

        JsonObject connectionArgs = new JsonObject();
        connectionArgs.addProperty("username", "testUser");
        connectionArgs.addProperty("password", "testPass");
        kwargs.add("connection_args", connectionArgs);

        kwargs.addProperty("import_success", true);
        kwargs.addProperty("import_error", "No error");
    }

    @Test
    public void testConstructor() {
        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertEquals("testUser", handler.getConnectionArgs().get("username").getAsString());
        assertEquals("testPass", handler.getConnectionArgs().get("password").getAsString());
        assertTrue(handler.isImportSuccess());
        assertEquals("No error", handler.getImportError());
    }

    @Test
    public void testConstructorWithNullValues() {
        kwargs.add("connection_args", null);
        kwargs.add("import_success", null);
        kwargs.add("import_error", null);

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNull(handler.getConnectionArgs());
        assertFalse(handler.isImportSuccess());
        assertNull(handler.getImportError());
    }

    @Test
    public void testToString() {
        Handler handler = new Handler(kwargs);
        assertEquals("Handler(testHandler)", handler.toString());
    }

    @Test
    public void testDescribe() {
        Handler handler = new Handler(kwargs);
        String expectedDescription = "Handler(name=testHandler, title=Test Handler, version=1.0, description=This is a test handler., connectionArgs={\"username\":\"testUser\",\"password\":\"testPass\"}, importSuccess=true, importError=No error)";
        assertEquals(expectedDescription, handler.describe());
    }

    @Test
    public void testConstructorWithMissingFields() {
        kwargs.remove("title");
        kwargs.remove("version");
        kwargs.remove("description");

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertNull(handler.getTitle());
        assertNull(handler.getVersion());
        assertNull(handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertEquals("testUser", handler.getConnectionArgs().get("username").getAsString());
        assertEquals("testPass", handler.getConnectionArgs().get("password").getAsString());
        assertTrue(handler.isImportSuccess());
        assertEquals("No error", handler.getImportError());
    }

    @Test
    public void testConstructorWithEmptyJsonObject() {
        Handler handler = new Handler(new JsonObject());

        assertNull(handler.getName());
        assertNull(handler.getTitle());
        assertNull(handler.getVersion());
        assertNull(handler.getDescription());
        assertNull(handler.getConnectionArgs());
        assertFalse(handler.isImportSuccess());
        assertNull(handler.getImportError());
    }

    @Test
    public void testConstructorWithInvalidConnectionArgs() {
        kwargs.add("connection_args", new JsonObject());

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertFalse(handler.getConnectionArgs().has("username"));
        assertFalse(handler.getConnectionArgs().has("password"));
        assertTrue(handler.isImportSuccess());
        assertEquals("No error", handler.getImportError());
    }

    @Test
    public void testConstructorWithBooleanImportSuccess() {
        kwargs.addProperty("import_success", false);

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertEquals("testUser", handler.getConnectionArgs().get("username").getAsString());
        assertEquals("testPass", handler.getConnectionArgs().get("password").getAsString());
        assertFalse(handler.isImportSuccess());
        assertEquals("No error", handler.getImportError());
    }

    @Test
    public void testConstructorWithNullImportError() {
        kwargs.add("import_error", null);

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertEquals("testUser", handler.getConnectionArgs().get("username").getAsString());
        assertEquals("testPass", handler.getConnectionArgs().get("password").getAsString());
        assertTrue(handler.isImportSuccess());
        assertNull(handler.getImportError());
    }

    @Test
    public void testConstructorWithEmptyImportError() {
        kwargs.addProperty("import_error", "");

        Handler handler = new Handler(kwargs);

        assertEquals("testHandler", handler.getName());
        assertEquals("Test Handler", handler.getTitle());
        assertEquals("1.0", handler.getVersion());
        assertEquals("This is a test handler.", handler.getDescription());
        assertNotNull(handler.getConnectionArgs());
        assertEquals("testUser", handler.getConnectionArgs().get("username").getAsString());
        assertEquals("testPass", handler.getConnectionArgs().get("password").getAsString());
        assertTrue(handler.isImportSuccess());
        assertEquals("", handler.getImportError());
    }
}