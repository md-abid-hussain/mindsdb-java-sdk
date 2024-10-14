package mindsdb.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Job;
import mindsdb.models.Project;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class JobsTest {
    private RestAPI api;
    private Project project;
    private Jobs jobs;

    @BeforeEach
    public void setUp() {
        api = mock(RestAPI.class);
        project = mock(Project.class);
        when(project.getName()).thenReturn("test_project");
        jobs = new Jobs(project, api);
    }

    @Test
    public void testCreateJobWithAllParameters() {
        LocalDateTime startAt = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2023, 10, 1, 12, 0);
        String queryStr = "SELECT * FROM slack.users";
        String repeatStr = "1 HOUR";
        Integer repeatMin = null;

        jobs.create("test_job", queryStr, startAt, endAt, repeatStr, repeatMin);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedQuery = String.format(
                "CREATE JOB test_job (%s) START '%s' END '%s' EVERY %s",
                queryStr,
                startAt.format(formatter),
                endAt.format(formatter),
                repeatStr);
        verify(api, times(1)).sqlQuery(expectedQuery, "test_project");
    }

    @Test
    public void testCreateJobWithRepeatMinutes() {
        LocalDateTime startAt = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2023, 10, 1, 12, 0);
        String queryStr = "SELECT * FROM table";
        String repeatStr = null;
        Integer repeatMin = 30;

        jobs.create("test_job", queryStr, startAt, endAt, repeatStr, repeatMin);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedQuery = String.format(
                "CREATE JOB test_job (%s) START '%s' END '%s' EVERY %d minutes",
                queryStr,
                startAt.format(formatter),
                endAt.format(formatter),
                repeatMin);
        verify(api, times(1)).sqlQuery(expectedQuery, "test_project");
    }

    @Test
    public void testCreateJobWithNoStartEnd() {
        String queryStr = "SELECT * FROM table";
        String repeatStr = "1 DAY";
        Integer repeatMin = null;

        jobs.create("test_job", queryStr, null, null, repeatStr, repeatMin);

        String expectedQuery = String.format(
                "CREATE JOB test_job (%s) EVERY %s",
                queryStr,
                repeatStr);
        verify(api, times(1)).sqlQuery(expectedQuery, "test_project");
    }

    @Test
    public void testCreateJobWithNoRepeat() {
        LocalDateTime startAt = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2023, 10, 1, 12, 0);
        String queryStr = "SELECT * FROM table";
        String repeatStr = null;
        Integer repeatMin = null;

        jobs.create("test_job", queryStr, startAt, endAt, repeatStr, repeatMin);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedQuery = String.format(
                "CREATE JOB test_job (%s) START '%s' END '%s'",
                queryStr,
                startAt.format(formatter),
                endAt.format(formatter));
        verify(api, times(1)).sqlQuery(expectedQuery, "test_project");
    }

    @Test
    public void testListJobs() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "QUERY", "START_AT", "END_AT", "SCHEDULE_STR" },
                new String[][] {
                        { "job1", "SELECT * FROM table", null, null, null },
                        { "job2", "SELECT * FROM table", null, null, null }
                });
        when(api.sqlQuery(anyString(), eq("test_project"))).thenReturn(mockResponse);

        List<Job> jobList = jobs.list();

        assertEquals(2, jobList.size());
        assertEquals("job1", jobList.get(0).name);
        assertEquals("job2", jobList.get(1).name);
    }

    @Test
    public void testGetJob() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "QUERY", "START_AT", "END_AT", "SCHEDULE_STR" },
                new String[][] {
                        { "job1", "SELECT * FROM table", null, null, null }
                });
        when(api.sqlQuery(anyString(), eq("test_project"))).thenReturn(mockResponse);

        Job job = jobs.get("job1");

        assertNotNull(job);
        assertEquals("job1", job.name);
    }

    @Test
    public void testGetNonExistentJob() {
        Table mockResponse = createMockTable(
                new String[] { "NAME", "QUERY", "START_AT", "END_AT", "SCHEDULE_STR" },
                new String[][] {});
        when(api.sqlQuery(anyString(), eq("test_project"))).thenReturn(mockResponse);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jobs.get("nonexistent");
        });

        String expectedMessage = "Job doesn't exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
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