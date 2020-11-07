package no.kristiania.database;

import no.kristiania.http.HttpMessage;
import no.kristiania.http.ProjectOptionsController;
import no.kristiania.http.UpdateWorkerController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectDaoTest {

    private ProjectDao projectDao;
    private static Random random = new Random();
    private WorkerDao workerDao;


    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        projectDao = new ProjectDao(dataSource);
        workerDao = new WorkerDao(dataSource);
    }

    @Test
    void shouldListAllProjects() throws SQLException {
        Project project1 = exampleProject();
        Project project2 = exampleProject();
        projectDao.insert(project1);
        projectDao.insert(project2);
        assertThat(projectDao.list())
                .extracting(Project::getName)
                .contains(project1.getName(), project2.getName());
    }

    @Test
    void shouldRetrieveAllProjectProperties() throws SQLException {
        Project project1 = exampleProject();
        Project project2 = exampleProject();
        Project project = exampleProject();
        projectDao.insert(project);
        assertThat(project).hasNoNullFieldsOrPropertiesExcept("workerId");
        assertThat(projectDao.retrieve(project.getId()))
                .usingRecursiveComparison()
                .isEqualTo(project);
    }

    @Test
    void shouldReturnProjectAsOptions() throws SQLException {
        ProjectOptionsController controller = new ProjectOptionsController(projectDao);
        Project project = ProjectDaoTest.exampleProject();
        projectDao.insert(project);

        assertThat(controller.getBody())
                .contains("<option value=" + project.getId() + ">" + project.getName() + "</option>");
    }

    @Test
    void shouldUpdateExistingProjectWithAssignedWorker() throws IOException, SQLException {
        UpdateWorkerController controller = new UpdateWorkerController(workerDao);

        Project project = exampleProject();
        projectDao.insert(project);

        Worker worker = WorkerDaoTest.exampleWorker();
        workerDao.insert(worker);

        String body = "projectId=" + project.getId() + "&workerId=" + worker.getId();
        controller.handle(new HttpMessage(body), null);
        assertThat(workerDao.retrieve(worker.getId()).getId())
                .isEqualTo(project.getId());
    }

    public static Project exampleProject() {
        Project project = new Project();
        project.setName(exampleProjectName());
        return project;
    }

    private static String exampleProjectName() {
        String[] options = { "Bedroom project", "Kitchen project", "Building project", "Park project"};
        return options[random.nextInt(options.length)];
    }
}
