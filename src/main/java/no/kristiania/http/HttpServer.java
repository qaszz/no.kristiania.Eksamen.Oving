package no.kristiania.http;

import no.kristiania.database.ProjectDao;
import no.kristiania.database.Worker;
import no.kristiania.database.WorkerDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HttpServer{

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    public static final String connection = "Connection: close";
    private Map<String, HttpController> controllers;

    private WorkerDao workerDao;
    private ServerSocket serverSocket;

    public HttpServer(int port, DataSource dataSource) throws IOException{
        workerDao = new WorkerDao(dataSource);
        ProjectDao projectDao = new ProjectDao(dataSource);
        controllers = Map.of(
                "/api/newProject", new ProjectPostController(projectDao),
                "/api/projects", new ProjectGetController(projectDao),
                "/api/projectOptions", new ProjectOptionsController(projectDao),
                "/api/workerOptions", new WorkerOptionsController(workerDao),
                "/api/updateProject", new UpdateWorkerController(workerDao)
        );
        serverSocket = new ServerSocket(port);
        logger.info("Server started on port {}", serverSocket.getLocalPort());

        new Thread(() ->{
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleRequest(Socket clientSocket) throws IOException, SQLException {
        HttpMessage request = new HttpMessage(clientSocket);
        String requestLine = request.getStartLine();
        System.out.println("REQUEST " + requestLine);
        String requestMethod = requestLine.split(" ")[0];
        String requestTarget = requestLine.split(" ")[1];



        int questionPos = requestTarget.indexOf('?');

        String requestPath = questionPos != -1 ? requestTarget.substring(0, questionPos) : requestTarget;

        if (requestMethod.equals("POST")){
            if (requestPath.equals("/api/workers")){
                handlePostProject(clientSocket, request);
            } else {
                getController(requestPath).handle(request, clientSocket);
            }
        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(clientSocket, requestTarget, questionPos);
            } else if (requestPath.equals("/api/projectworkers")){
                handleGetWorkers(clientSocket, requestTarget, questionPos);
            } else {
                HttpController controller = controllers.get(requestPath);
                if (controller != null) {
                    controller.handle(request, clientSocket);
                } else {
                    handleFileRequest(clientSocket, requestPath);
                }
            }
        }
    }

    private HttpController getController(String requestPath) {
        return controllers.get(requestPath);
    }

    private void handlePostProject(Socket clientSocket, HttpMessage request) throws SQLException, IOException {
        HttpMessage response = handlePostProject(request);
        response.write(clientSocket);
    }

    private HttpMessage handlePostProject(HttpMessage request) throws SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Worker worker = new Worker();
        worker.setName(requestParameter.getParameter("worker_name"));
        worker.setEmail(requestParameter.getParameter("email_address"));
        workerDao.insert(worker);
        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 Redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/workers.html");
        return redirect;
    }

    public int getPort(){
        return serverSocket.getLocalPort();
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

    private void handleGetWorkers(Socket clientSocket, String requestTarget, int questionPos) throws IOException, SQLException {
        Integer projectId = null;
        if (questionPos != -1){
            projectId = Integer.valueOf(new QueryString(requestTarget.substring(questionPos+1))
                    .getParameter("projectId"));
        }
        List<Worker> list = projectId == null ? workerDao.list() : workerDao.listWorkersByProjectId(projectId);
        String body = "<ul>";
        for (Worker worker : list) {
            body += "<li>Name: " + worker.getName() + "<br> Email Address: " + worker.getEmail() + "</li>";

        }

        body+= "</ul>";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n"+
                connection + "\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleEchoRequest(Socket clientSocket, String requestTarget, int questionPos) throws IOException {
        String statusCode = "200";
        String body = "Hello <strong>World</strong>!";
        if (questionPos != -1) {
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
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        logger.info("Using database {}", dataSource.getUrl());
        Flyway.configure().dataSource(dataSource).load().migrate();

        HttpServer server = new HttpServer(8080, dataSource);
        logger.info("Started on http://localhost:{}/index.html", 8080);

    }

    public List<Worker> getWorkers() throws SQLException {
        return workerDao.list();
    }
}