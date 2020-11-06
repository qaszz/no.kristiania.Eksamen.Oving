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
                    "INSERT INTO workers (worker_name, email) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, worker.getName());
                statement.setString(2, worker.getEmail());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    worker.setId(generatedKeys.getLong("id"));
                }
            }
        }
    }

    public Worker retrieve(Long id) throws SQLException {
        return retrieve(id, "SELECT * FROM workers WHERE id = ?");
    }

    @Override
    protected Worker mapRow(ResultSet rs) throws SQLException {
        Worker worker = new Worker();
        worker.setId(rs.getLong("id"));
        worker.setName(rs.getString("worker_name"));
        worker.setEmail(rs.getString("email"));
        return worker;
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
}
