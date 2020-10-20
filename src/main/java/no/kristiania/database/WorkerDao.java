package no.kristiania.database;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WorkerDao {

    private DataSource dataSource;

    public WorkerDao(DataSource dataSource) {

        this.dataSource = dataSource;
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
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workers WHERE id = ?")) {
                statement.setLong(1,id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Worker worker = new Worker();
                        worker.setId(rs.getLong("id"));
                        worker.setName(rs.getString("worker_name"));
                        worker.setEmail(rs.getString("email"));
                        return worker;
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    public List<String> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workers")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<String> workers = new ArrayList<>();
                    while (rs.next()) {
                        workers.add(rs.getString("worker_name"));
                    }
                    return workers;
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiacompany");
        dataSource.setUser("kristianiaboss");
        // TODO: database passwords should never be checked in!
        dataSource.setPassword("hermosa321");

        WorkerDao workerDao = new WorkerDao(dataSource);

        System.out.println("What's the name of the new worker");
        Scanner scanner = new Scanner(System.in);

        Worker worker = new Worker();
        worker.setName(scanner.nextLine());

        workerDao.insert(worker);
        System.out.println(workerDao.list());
    }
}
