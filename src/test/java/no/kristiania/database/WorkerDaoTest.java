package no.kristiania.database;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
class WorkerDaoTest {

    @Test
    void shouldListInsertedWorkers() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();

        WorkerDao workerDao = new WorkerDao(dataSource);
        String worker = exampleWorkerName();
        workerDao.insert(worker);
        assertThat(workerDao.list()).contains(worker);
    }

    private String exampleWorkerName() {
        String[] options = { "Ole", "Ali", "Chris", "Bjørn"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }
}