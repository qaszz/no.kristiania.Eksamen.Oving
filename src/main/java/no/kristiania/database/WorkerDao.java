package no.kristiania.database;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class WorkerDao {
    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiacompany");
        dataSource.setUser("kristianiaboss");
        // TODO: database passwords should never be checked in!
        dataSource.setPassword("hermosa321");

        System.out.println("What's the name of the new worker");
        Scanner scanner = new Scanner(System.in);
        String workerName = scanner.nextLine();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO workers (worker_name) values (?)")) {
                statement.setString(1, workerName);
                statement.executeUpdate();
            }
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workers")) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getString("worker_name"));
                    }
                }
            }
        }
    }

    public void insert(String worker) {
        
    }

    public List<String> list() {
        return null;
    }
}
