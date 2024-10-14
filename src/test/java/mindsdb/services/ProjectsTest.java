package mindsdb.services;

import java.util.List;

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
import mindsdb.models.Project;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ProjectsTest {
    private RestAPI api;
    private Projects projects;

    @BeforeEach
    public void setUp() {
        api = mock(RestAPI.class);
        Server server = mock(Server.class);
        projects = new Projects(server, api);
    }

    @Test
    public void testListProjects() {
        Table mockResponse = createMockTable(
                new String[] { "NAME" },
                new String[][] {
                        { "project1" },
                        { "project2" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        List<Project> projectList = projects.list();

        assertEquals(2, projectList.size());
        assertEquals("project1", projectList.get(0).getName());
        assertEquals("project2", projectList.get(1).getName());
    }

    @Test
    public void testGetProject() {
        Table mockResponse = createMockTable(
                new String[] { "NAME" },
                new String[][] {
                        { "project1" },
                        { "project2" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        Project project = projects.get("project1");

        assertNotNull(project);
        assertEquals("project1", project.getName());
    }

    @Test
    public void testGetNonExistentProject() {
        Table mockResponse = createMockTable(
                new String[] { "NAME" },
                new String[][] {
                        { "project1" },
                        { "project2" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projects.get("nonexistent");
        });

        String expectedMessage = "Project doesn't exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateProject() {
        Table mockResponse = createMockTable(
                new String[] { "NAME" },
                new String[][] {
                        { "new_project" }
                });
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        Project project = projects.create("new_project");

        assertNotNull(project);
        assertEquals("new_project", project.getName());
    }

    @Test
    public void testDropProject() {
        Table mockResponse = createMockTable(
                new String[] { "NAME" },
                new String[][] {});
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        projects.drop("project_to_drop");

        verify(api, times(1)).sqlQuery("DROP DATABASE project_to_drop");
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