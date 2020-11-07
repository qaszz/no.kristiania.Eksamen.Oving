package no.kristiania.database;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerDao extends AbstractDao<Worker>{


    public WorkerDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(Worker worker) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO workers (worker_name, email, project_id) values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, worker.getName());
                statement.setString(2, worker.getEmail());
                statement.setObject(3, worker.getProjectId());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    worker.setId(generatedKeys.getInt("id"));
                }
            }
        }
    }

    public void update(Worker worker) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE workers set project_id = ? WHERE id = ?"
            )) {
                statement.setInt(1, worker.getProjectId());
                statement.setInt(2, worker.getId());
                statement.executeUpdate();
            }
        }
    }


    public Worker retrieve(Integer id) throws SQLException {
        return retrieve(id, "SELECT * FROM workers WHERE id = ?");
    }


    public List<Worker> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workers")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Worker> workers = new ArrayList<>();
                    while (rs.next()) {
                        workers.add(mapRow(rs));
                    }
                    return workers;
                }
            }
        }
    }

    public List<Worker> listWorkersByProjectId(Integer projectId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workers WHERE project_id = ?")) {
                statement.setInt(1, projectId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Worker> workers = new ArrayList<>();
                    while (rs.next()) {
                        workers.add(mapRow(rs));
                    }
                    return workers;
                }
            }
        }
    }

    @Override
    protected Worker mapRow(ResultSet rs) throws SQLException {
        Worker worker = new Worker();
        worker.setId(rs.getInt("id"));
        worker.setProjectId(rs.getInt("project_id"));
        worker.setName(rs.getString("worker_name"));
        worker.setEmail(rs.getString("email"));
        return worker;
    }


}
