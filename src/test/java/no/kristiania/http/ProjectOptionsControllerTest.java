package no.kristiania.http;

import no.kristiania.database.Project;
import no.kristiania.database.ProjectDao;
import no.kristiania.database.ProjectDaoTest;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectOptionsControllerTest {

    private ProjectDao projectDao;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        projectDao = new ProjectDao(dataSource);
    }


    @Test
    void shouldReturnProjectAsOptions() throws SQLException {
        ProjectOptionsController controller = new ProjectOptionsController(projectDao);
        Project project = ProjectDaoTest.exampleProject();
        projectDao.insert(project);

        assertThat(controller.getBody())
                .contains("<option value=" + project.getId() + ">" + project.getName() + "</option>");
    }

}