package mindsdb.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;

public class ServerTest {

    @Mock
    private RestAPI api;

    private Server server;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(api);
    }

    @Test
    public void testStatus() {
        JsonObject mockStatus = mock(JsonObject.class);
        when(api.status()).thenReturn(mockStatus);

        JsonObject status = server.status();
        assertNotNull(status);
    }

    @Test
    public void testToString() {
        when(api.getUrl()).thenReturn("http://localhost:47334");

        String result = server.toString();
        assertEquals("Server(http://localhost:47334)", result);
    }

    @Test
    public void testGetApi() {
        RestAPI restapi = server.getApi();
        assertNotNull(restapi);
    }
}