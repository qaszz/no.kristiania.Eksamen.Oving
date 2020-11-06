package no.kristiania.database;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class StatusDaoTest {

    private StatusDao statusDao;
    private Random random = new Random();

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        statusDao = new StatusDao(dataSource);
    }

    @Test
    void shouldListAllStatuses() throws SQLException {
        Status status1 = exampleStatus();
        Status status2 = exampleStatus();
        statusDao.insert(status1);
        statusDao.insert(status2);
        assertThat(statusDao.list())
                .extracting(Status::getStatusName)
                .contains(status1.getStatusName(), status2.getStatusName());
    }

    @Test
    void shouldRetrieveAllStatusProperties() throws SQLException {
        Status status2 = exampleStatus();
        Status status1 = exampleStatus();
        Status status = exampleStatus();
        statusDao.insert(status);
        assertThat(status).hasNoNullFieldsOrProperties();
        assertThat(statusDao.retrieve(status.getId()))
                .usingRecursiveComparison()
                .isEqualTo(status);
    }

    private Status exampleStatus() {
         Status status = new Status();
         status.setStatusName(exampleStatusName());
         return status;
    }

    private String exampleStatusName() {
        String[] options = { "Bedroom project", "Kitchen project", "Building project", "Park project"};
        return options[random.nextInt(options.length)];
    }


}