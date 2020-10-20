package no.kristiania.http;

import no.kristiania.database.WorkerDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    public static final String connection = "Connection: close";

    private final WorkerDao workerDao;

    public HttpServer(int port, DataSource dataSource) throws IOException{
        workerDao = new WorkerDao(dataSource);
        // Opens an entry point to our program for network clients
        ServerSocket serverSocket = new ServerSocket(port);

        // new Thread executes the code in a separate "thread", that is: In parallel
        new Thread(() ->{ // anonymous function with code that will be executed in parallel
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) { //accept waits for a client to try to connect - blocks
                    handleRequest(clientSocket);
                } catch (IOException | SQLException e) {
                    // If something went wrong - print out exception and try again
                    e.printStackTrace();
                }
            }
        }).start(); // Start the threads, so the code inside executes without blocking the current thread

    }

    // This code will be executed for each client
    private void handleRequest(Socket clientSocket) throws IOException, SQLException {
        HttpMessage request = new HttpMessage(clientSocket);
        String requestLine = request.getStartLine();
        System.out.println("REQUEST " + requestLine);
        // Example GET /index.html HTTP/1.1
        String requestMethod = requestLine.split(" ")[0];

        String requestTarget = requestLine.split(" ")[1];
        // Example GET /echo?body=hello HTTP/1.1




        int questionPos = requestTarget.indexOf('?');

        String requestPath = questionPos != -1 ? requestTarget.substring(0, questionPos) : requestTarget;

        if (requestMethod.equals("POST")){
            QueryString requestParameter = new QueryString(request.getBody());

            workerDao.insert(requestParameter.getParameter("full_name"));
            String body = "You have added a new worker!";
            String response = "HTTP/1.1 200 OK\r\n" +
                    connection + "\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body;

            // Write the response back to the client
            clientSocket.getOutputStream().write(response.getBytes());

        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(clientSocket, requestTarget, questionPos);

            } else if (requestPath.equals("/api/projectMembers")){
                handleGetMembers(clientSocket);
            } else {
                handleFileRequest(clientSocket, requestPath);
            }
        }
    }

    private void handleFileRequest(Socket clientSocket, String requestPath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(requestPath)) {
           if (inputStream == null) {
               String body = requestPath + " does not exist";
               String response = "HTTP/1.1 404 Not Found\r\n" +
                       "Content-Length: " + body.length() + "\r\n" +
                       connection + "\r\n" +
                       "\r\n" +
                       body;

               // Write the response back to the client
               clientSocket.getOutputStream().write(response.getBytes());
               return;
           }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);
            String contentType = "text/plain";
            if (requestPath.endsWith(".html")){
                contentType = "text/html";
            } else if (requestPath.endsWith(".css")) {
                contentType = "text/plain";
            }

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + buffer.toByteArray().length + "\r\n" +
                    connection + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "\r\n";
            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.getOutputStream().write(buffer.toByteArray());
        }
    }

    private void handleGetMembers(Socket clientSocket) throws IOException, SQLException {
        String body = "<ul>";
        for (String member : workerDao.list()) {
            body += "<li>" + member + "</li>";
        }

        body+= "</ul>";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n"+
                connection + "\r\n" +
                "\r\n" +
                body;

        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleEchoRequest(Socket clientSocket, String requestTarget, int questionPos) throws IOException {
        String statusCode = "200";
        String body = "Hello <strong>World</strong>!";
        if (questionPos != -1) {
            // body = helloo
            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            if (queryString.getParameter("status") != null) {
                statusCode = queryString.getParameter("status");
            }
            if (queryString.getParameter("body") != null) {
                body = queryString.getParameter("body");
            }
        }
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/plain\r\n" +
                connection + "\r\n" +
                "\r\n" +
                body;

        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.username"));
        // TODO: database passwords should never be checked in!
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        logger.info("Using database {}", dataSource.getUrl());
        Flyway.configure().dataSource(dataSource).load().migrate();

        HttpServer server = new HttpServer(8080, dataSource);
        logger.info("Started on http://localhost:{}/index.html", 8080);

    }

    public List<String> getMembers() throws SQLException {
        return workerDao.list();
    }
}