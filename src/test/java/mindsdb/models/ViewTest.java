package mindsdb.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ViewTest {
    @Mock
    private Project mockProject;

    private View view;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        view = new View(mockProject, "testView");
    }

    @Test
    public void testConstructor() {
        assertNotNull(view);
        assertEquals("testView", view.getName());
        assertEquals(mockProject, view.getProject());
    }

    @Test
    public void testGetProject() {
        assertEquals(mockProject, view.getProject());
    }

    @Test
    public void testGetName() {
        assertEquals("testView", view.getName());
    }
}