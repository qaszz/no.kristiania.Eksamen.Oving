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

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        workerDao = new WorkerDao(dataSource);
    }

    @Test
    void shouldListInsertedWorkers() throws SQLException {
        Worker worker = exampleWorker();
        workerDao.insert(worker);
        assertThat(workerDao.list()).contains(worker.getName());
    }

    @Test
    void shouldRetrieveAllWorkerProperties() throws SQLException {
        Worker worker = exampleWorker();
        workerDao.insert(worker);
        assertThat(worker).hasNoNullFieldsOrProperties();
        assertThat(workerDao.retrieve(worker.getId()))
                .usingRecursiveComparison()
                .isEqualTo(worker);
    }

    private Worker exampleWorker() {
        return new Worker();
    }

    private String exampleWorkerName() {
        String[] options = { "Ole", "Ali", "Chris", "Bjørn"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }
}