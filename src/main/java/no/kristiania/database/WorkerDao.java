package no.kristiania.database;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WorkerDao {

    private final ArrayList<String> workers = new ArrayList<>();
    private DataSource dataSource;

    public WorkerDao(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public void insert(String worker) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO workers (worker_name) values (?)")) {
                statement.setString(1, worker);
                statement.executeUpdate();
            }
        }

        workers.add(worker);
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
        String workerName = scanner.nextLine();

        workerDao.insert(workerName);
        System.out.println(workerDao.list());
    }
}
