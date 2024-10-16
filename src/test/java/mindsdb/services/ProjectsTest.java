package mindsdb.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private Server server;
    private Projects projects;

    @BeforeEach
    public void setUp() {
        api = mock(RestAPI.class);
        server = mock(Server.class);
        projects = new Projects(server, api);
    }

    @Test
    public void testListProjects() {
        // Create a mock response table
        Table mockResponse = Table.create("databases")
                .addColumns(
                        StringColumn.create("NAME", new String[] { "project1", "project2" }));

        // Mock the API call to return the mock response
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        // Call the method under test
        List<Project> projectList = projects.list();

        // Verify the results
        assertNotNull(projectList);
        assertEquals(2, projectList.size());
        assertEquals("project1", projectList.get(0).getName());
        assertEquals("project2", projectList.get(1).getName());
    }

    @Test
    public void testGetProject() {
        // Create a mock response table
        Table mockResponse = Table.create("databases")
                .addColumns(
                        StringColumn.create("NAME", new String[] { "project1", "project2" }));

        // Mock the API call to return the mock response
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        // Call the method under test
        Project project = projects.get("project1");

        // Verify the results
        assertNotNull(project);
        assertEquals("project1", project.getName());
    }

    @Test
    public void testGetNonExistentProject() {
        // Create a mock response table
        Table mockResponse = Table.create("databases")
                .addColumns(
                        StringColumn.create("NAME", new String[] { "project1", "project2" }));

        // Mock the API call to return the mock response
        when(api.sqlQuery(anyString())).thenReturn(mockResponse);

        // Call the method under test and verify the exception
        assertThrows(IllegalArgumentException.class, () -> projects.get("nonexistent_project"));
    }

    @Test
    public void testCreateProject() {
        // Mock the API call to return an empty response
        when(api.sqlQuery(anyString())).thenReturn(Table.create("empty"));

        // Call the method under test
        Project project = projects.create("new_project");

        // Verify the results
        assertNotNull(project);
        assertEquals("new_project", project.getName());

        // Verify that the correct SQL query was executed
        verify(api, times(1)).sqlQuery("CREATE DATABASE new_project WITH ENGINE 'mindsdb'");
    }

    @Test
    public void testDropProject() {
        // Mock the API call to return an empty response
        when(api.sqlQuery(anyString())).thenReturn(Table.create("empty"));

        // Call the method under test
        projects.drop("project_to_drop");

        // Verify that the correct SQL query was executed
        verify(api, times(1)).sqlQuery("DROP DATABASE project_to_drop");
    }
}