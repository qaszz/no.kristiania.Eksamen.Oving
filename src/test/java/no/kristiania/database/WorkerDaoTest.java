package no.kristiania.database;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
class WorkerDaoTest {

    private WorkerDao workerDao;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        workerDao = new WorkerDao(dataSource);
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

    private Worker exampleWorker() {
        Worker worker = new Worker();
        worker.setName(exampleWorkerName());
        worker.setEmail("username" + random.nextInt(1000) + "@gmail.com");
        return worker;
    }

    private String exampleWorkerName() {
        String[] options = { "Ole", "Ali", "Chris", "Bjørn"};
        return options[random.nextInt(options.length)];
    }
}