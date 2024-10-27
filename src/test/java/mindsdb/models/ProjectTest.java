package mindsdb.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import mindsdb.connectors.RestAPI;
import mindsdb.services.Jobs;
import mindsdb.services.KnowledgeBases;
import mindsdb.services.Models;
import mindsdb.services.Query;
import mindsdb.services.Views;

public class ProjectTest {

    @Mock
    private RestAPI api;
    private Project project;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        project = new Project(api, "testProject");
    }

    @Test
    public void testConstructor() {
        assertEquals("testProject", project.getName());
        assertNotNull(project.getApi());
        assertNotNull(project.getJobs());
        assertNotNull(project.getViews());
        assertNotNull(project.getModels());
        assertNotNull(project.getKnowledgeBases());
        assertNotNull(project.getSkills());
        assertNotNull(project.getDatabases());
        assertNotNull(project.getMlEngines());
    }

    @Test
    public void testQuery() {
        Query query = project.query("SELECT * FROM test_table");
        assertNotNull(query);
        assertEquals("SELECT * FROM test_table", query.getSql());
        assertEquals("testProject", query.getDatabase());
    }

    @Test
    public void testDropModelVersion() {
        Models mockModels = mock(Models.class);

        try {
            java.lang.reflect.Field modelsField = Project.class.getDeclaredField("models");
            modelsField.setAccessible(true);
            modelsField.set(project, mockModels);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the models field: " + e.getMessage());
        }

        project.dropModelVersion("testModel", 1);

        verify(mockModels, times(1)).dropVersion("testModel", 1);
    }

    @Test
    public void testToString() {
        assertEquals("Project(testProject)", project.toString());
    }

    @Test
    public void testListJobs() {
        Jobs mockJobs = mock(Jobs.class);
        when(mockJobs.list()).thenReturn(List.of(new Job(project, "job1", null, null)));
        try {
            java.lang.reflect.Field jobsField = Project.class.getDeclaredField("jobs");
            jobsField.setAccessible(true);
            jobsField.set(project, mockJobs);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the jobs field: " + e.getMessage());
        }

        List<Job> jobs = project.listJobs();
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("job1", jobs.get(0).getName());
    }

    @Test
    public void testGetJob() {
        Jobs mockJobs = mock(Jobs.class);
        Job mockJob = mock(Job.class);
        when(mockJobs.get("job1")).thenReturn(mockJob);
        try {
            java.lang.reflect.Field jobsField = Project.class.getDeclaredField("jobs");
            jobsField.setAccessible(true);
            jobsField.set(project, mockJobs);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the jobs field: " + e.getMessage());
        }

        Job job = project.getJob("job1");
        assertNotNull(job);
        assertEquals(mockJob, job);
    }

    @Test
    public void testCreateJob() {
        Jobs mockJobs = mock(Jobs.class);
        Job mockJob = mock(Job.class);
        when(mockJobs.create(anyString(), anyString(), any(), any(), any(), any())).thenReturn(mockJob);
        try {
            java.lang.reflect.Field jobsField = Project.class.getDeclaredField("jobs");
            jobsField.setAccessible(true);
            jobsField.set(project, mockJobs);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the jobs field: " + e.getMessage());
        }

        Job job = project.createJob("job1", "SELECT * FROM test_table", LocalDateTime.now(), LocalDateTime.now(),
                "0 0 * * *", 10);
        assertNotNull(job);
        assertEquals(mockJob, job);
    }

    @Test
    public void testListViews() {
        Views mockViews = mock(Views.class);
        when(mockViews.list()).thenReturn(List.of(new View(project, "view1")));
        try {
            java.lang.reflect.Field viewsField = Project.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            viewsField.set(project, mockViews);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the views field: " + e.getMessage());
        }

        List<View> views = project.listViews();
        assertNotNull(views);
        assertEquals(1, views.size());
        assertEquals("view1", views.get(0).getName());
    }

    @Test
    public void testGetView() {
        Views mockViews = mock(Views.class);
        View mockView = mock(View.class);
        when(mockViews.get("view1")).thenReturn(mockView);
        try {
            java.lang.reflect.Field viewsField = Project.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            viewsField.set(project, mockViews);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the views field: " + e.getMessage());
        }

        View view = project.getView("view1");
        assertNotNull(view);
        assertEquals(mockView, view);
    }

    @Test
    public void testCreateView() {
        Views mockViews = mock(Views.class);
        View mockView = mock(View.class);
        when(mockViews.create(anyString(), any(Query.class))).thenReturn(mockView);
        try {
            java.lang.reflect.Field viewsField = Project.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            viewsField.set(project, mockViews);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the views field: " + e.getMessage());
        }

        Query query = new Query(api, "SELECT * FROM test_table", "testProject");
        View view = project.createView("view1", query);
        assertNotNull(view);
        assertEquals(mockView, view);
    }

    @Test
    public void testDropView() {
        Views mockViews = mock(Views.class);
        try {
            java.lang.reflect.Field viewsField = Project.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            viewsField.set(project, mockViews);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the views field: " + e.getMessage());
        }

        project.dropView("view1");

        verify(mockViews, times(1)).drop("view1");
    }

    @Test
    public void testListModels() {
        Models mockModels = mock(Models.class);
        when(mockModels.listModels(null)).thenReturn(List.of(new Model(project, Map.of())));
        try {
            java.lang.reflect.Field modelsField = Project.class.getDeclaredField("models");
            modelsField.setAccessible(true);
            modelsField.set(project, mockModels);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the models field: " + e.getMessage());
        }

        List<Model> models = project.listModels();
        assertNotNull(models);
        assertEquals(1, models.size());
    }

    @Test
    public void testGetModel() {
        Models mockModels = mock(Models.class);
        Model mockModel = mock(Model.class);
        when(mockModels.getModel("model1")).thenReturn(mockModel);
        try {
            java.lang.reflect.Field modelsField = Project.class.getDeclaredField("models");
            modelsField.setAccessible(true);
            modelsField.set(project, mockModels);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the models field: " + e.getMessage());
        }

        Model model = project.getModel("model1");
        assertNotNull(model);
        assertEquals(mockModel, model);
    }

    @Test
    public void testListKnowledgeBases() {
        KnowledgeBases mockKnowledgeBases = mock(KnowledgeBases.class);
        when(mockKnowledgeBases.list()).thenReturn(List.of(new KnowledgeBase(api, project, Map.of())));
        try {
            java.lang.reflect.Field knowledgeBasesField = Project.class.getDeclaredField("knowledgeBases");
            knowledgeBasesField.setAccessible(true);
            knowledgeBasesField.set(project, mockKnowledgeBases);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the knowledgeBases field: " + e.getMessage());
        }

        List<KnowledgeBase> knowledgeBases = project.listKnowledgeBases();
        assertNotNull(knowledgeBases);
        assertEquals(1, knowledgeBases.size());
    }

    @Test
    public void testGetKnowledgeBase() {
        KnowledgeBases mockKnowledgeBases = mock(KnowledgeBases.class);
        KnowledgeBase mockKnowledgeBase = mock(KnowledgeBase.class);
        when(mockKnowledgeBases.get("kb1")).thenReturn(mockKnowledgeBase);
        try {
            java.lang.reflect.Field knowledgeBasesField = Project.class.getDeclaredField("knowledgeBases");
            knowledgeBasesField.setAccessible(true);
            knowledgeBasesField.set(project, mockKnowledgeBases);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set the knowledgeBases field: " + e.getMessage());
        }

        KnowledgeBase knowledgeBase = project.getKnowledgeBase("kb1");
        assertNotNull(knowledgeBase);
        assertEquals(mockKnowledgeBase, knowledgeBase);
    }
}
