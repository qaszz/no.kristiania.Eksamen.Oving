package no.kristiania.database;

import no.kristiania.http.HttpMessage;
import no.kristiania.http.UpdateWorkerController;
import no.kristiania.http.WorkerOptionsController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
class WorkerDaoTest {

    private WorkerDao workerDao;
    private static Random random = new Random();
    private ProjectDao projectDao;
    private Project defaultProject;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        workerDao = new WorkerDao(dataSource);
        projectDao = new ProjectDao(dataSource);

        defaultProject = ProjectDaoTest.exampleProject();
        projectDao.insert(defaultProject);
    }

    @Test
    void shouldListInsertedWorkers() throws SQLException {
        Worker worker1 = exampleWorker();
        Worker worker2 = exampleWorker();
        workerDao.insert(worker1);
        workerDao.insert(worker2);
        assertThat(workerDao.list())
                .extracting(Worker::getName)
                .contains(worker1.getName(), worker2.getName());
    }

    @Test
    void shouldListWorkersByProjects() throws SQLException {
        Project project1 = ProjectDaoTest.exampleProject();;
        projectDao.insert(project1);
        Project project2 = ProjectDaoTest.exampleProject();;
        projectDao.insert(project2);

        Worker matchingWorker = exampleWorker();
        matchingWorker.setProjectId(project1.getId());
        workerDao.insert(matchingWorker);
        Worker nonMatchingWorker = exampleWorker();
        nonMatchingWorker.setProjectId(project2.getId());
        workerDao.insert(nonMatchingWorker);

        assertThat(workerDao.listWorkersByProjectId(project1.getId()))
                .extracting(Worker::getId)
                .contains(matchingWorker.getId())
                .doesNotContain(nonMatchingWorker.getId());
    }

    @Test
    void shouldRetrieveAllWorkerProperties() throws SQLException {
        workerDao.insert(exampleWorker());
        workerDao.insert(exampleWorker());
        Worker worker = exampleWorker();
        workerDao.insert(worker);
        assertThat(worker).hasNoNullFieldsOrProperties();
        assertThat(workerDao.retrieve(worker.getId()))
                .usingRecursiveComparison()
                .isEqualTo(worker);
    }

    @Test
    void shouldReturnWorkerAsOptions() throws SQLException {
        WorkerOptionsController controller = new WorkerOptionsController(workerDao);
        Worker worker = exampleWorker();
        workerDao.insert(worker);

        assertThat(controller.getBody())
                .contains("<option value=" + worker.getId() + ">" + worker.getName() + "</option>");
    }

    @Test
    void shouldUpdateExistingProjectWithAssignedWorker() throws IOException, SQLException {
        UpdateWorkerController controller = new UpdateWorkerController(workerDao);


        Worker worker = exampleWorker();
        workerDao.insert(worker);

        Project project = ProjectDaoTest.exampleProject();
        projectDao.insert(project);

        String body = "projectId=" + project.getId() + "&workerId=" + worker.getId();
        HttpMessage response = controller.handle(new HttpMessage(body));
        assertThat(workerDao.retrieve(worker.getId()).getProjectId())
                .isEqualTo(project.getId());
        assertThat(response.getStartLine())
                .isEqualTo("HTTP/1.1 302 Redirect");
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("http://localhost:8080/index.html");
    }


    private Worker exampleWorker() {
        Worker worker = new Worker();
        worker.setName(exampleWorkerName());
        worker.setEmail("username" + random.nextInt(1000) + "@gmail.com");
        worker.setProjectId(defaultProject.getId());
        return worker;
    }

    private static String exampleWorkerName() {
        String[] options = { "Ole", "Ali", "Chris", "Bjørn"};
        return options[random.nextInt(options.length)];
    }
}