package no.kristiania.database;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    private DataSource dataSource;

    public ProjectDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Project> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM projects")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Project> projects = new ArrayList<>();
                    while (rs.next()) {
                        projects.add(mapRowToProject(rs));
                    }
                    return projects;
                }
            }
        }
    }

    private Project mapRowToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setName(rs.getString("name"));
        return project;
    }

    public void insert(Project project) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO projects (name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, project.getName());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    project.setId(generatedKeys.getLong("id"));
                }
            }
        }
    }

    public Project retrieve(long id) {
        return null;
    }
}
