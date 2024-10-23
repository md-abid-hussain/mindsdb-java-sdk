package mindsdb.models;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Query;
import mindsdb.utils.CreateJobCallback;
import tech.tablesaw.api.Table;

public class JobTest {

    @Mock
    private Project project;

    @Mock
    private CreateJobCallback createCallback;

    @Mock
    private RestAPI api;

    private Job job;
    private Map<String, String> data;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        data = new HashMap<>();
        data.put("query", "SELECT * FROM test_table");
        data.put("start_at", "2023-01-01T00:00:00Z");
        data.put("end_at", "2023-01-02T00:00:00Z");
        data.put("next_run_at", "2023-01-03T00:00:00Z");
        data.put("schedule_str", "0 0 * * *");
        job = new Job(project, "testJob", data, createCallback);
        when(project.getApi()).thenReturn(api);
        when(project.getName()).thenReturn("testProject");
    }

    @Test
    public void testConstructorWithData() {
        assertEquals("testJob", job.getName());
        assertEquals("SELECT * FROM test_table", job.getQueryString());
        assertEquals("2023-01-01T00:00:00Z", job.getStartAt());
        assertEquals("2023-01-02T00:00:00Z", job.getEndAt());
        assertEquals("2023-01-03T00:00:00Z", job.getNextRunAt());
        assertEquals("0 0 * * *", job.getScheduleStr());
        assertNotNull(job.getData());
        assertNotNull(job.getQueries());
        assertTrue(job.getQueries().isEmpty());
    }

    @Test
    public void testConstructorWithoutData() {
        Job jobWithoutData = new Job(project, "testJobWithoutData", createCallback);
        assertEquals("testJobWithoutData", jobWithoutData.getName());
        assertNull(jobWithoutData.getQueryString());
        assertNull(jobWithoutData.getStartAt());
        assertNull(jobWithoutData.getEndAt());
        assertNull(jobWithoutData.getNextRunAt());
        assertNull(jobWithoutData.getScheduleStr());
        assertNull(jobWithoutData.getData());
        assertNotNull(jobWithoutData.getQueries());
        assertTrue(jobWithoutData.getQueries().isEmpty());
    }

    @Test
    public void testToString() {
        assertEquals("Job(testJob, query='SELECT * FROM test_table')", job.toString());
    }

    @Test
    public void testExitWithoutQueries() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            job.exit();
        });
        assertEquals("No queries were added to job", exception.getMessage());
    }

    @Test
    public void testExitWithQueries() {
        job.addQuery("SELECT * FROM another_table");
        job.exit();
        verify(createCallback, times(1)).execute("SELECT * FROM another_table");
        verify(project, times(1)).getJob("testJob");
    }

    @Test
    public void testClose() throws Exception {
        job.addQuery("SELECT * FROM another_table");
        job.close();
        verify(createCallback, times(1)).execute("SELECT * FROM another_table");
        verify(project, times(1)).getJob("testJob");
    }

    @Test
    public void testRefresh() {
        Job refreshedJob = mock(Job.class);
        when(project.getJob("testJob")).thenReturn(refreshedJob);
        when(refreshedJob.getData()).thenReturn(data);
        job.refresh();
        verify(project, times(1)).getJob("testJob");
    }

    @Test
    public void testAddQueryWithQueryObject() {
        Query query = mock(Query.class);
        when(query.getDatabase()).thenReturn("testProject");
        job.addQuery(query);
        assertTrue(job.getQueries().contains(query.getSql()));
    }

    @Test
    public void testAddQueryWithQueryObjectWrongDatabase() {
        Query query = mock(Query.class);
        when(query.getDatabase()).thenReturn("wrongDatabase");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            job.addQuery(query);
        });
        assertEquals("Wrong query database: wrongDatabase. You could try to use SQL string instead",
                exception.getMessage());
    }

    @Test
    public void testAddQueryWithString() {
        job.addQuery("SELECT * FROM another_table");
        assertTrue(job.getQueries().contains("SELECT * FROM another_table"));
    }

    @Test
    public void testGetHistory() {
        Table mockTable = mock(Table.class);
        when(api.sqlQuery(anyString(), anyString())).thenReturn(mockTable);

        Table history = job.getHistory();

        assertNotNull(history);
        verify(api, times(1)).sqlQuery("SELECT * FROM log.jobs_history WHERE name = 'testJob'", "testProject");
    }
}