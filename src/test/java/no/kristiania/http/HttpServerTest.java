package no.kristiania.http;

import no.kristiania.database.Worker;
import no.kristiania.database.WorkerDao;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private JdbcDataSource dataSource;
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();

        server = new HttpServer(0, dataSource);
    }

    @Test
    void shouldReturnSuccessfulStatusCode() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccessfulStatusCode() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnContentLength() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=HelloWorld");
        assertEquals("HelloWorld", client.getResponseBody());
    }

    @Test
    void shouldReturnFileFromDisk() throws IOException{
        File contentRoot = new File("target/test-classes");

        String fileContent = "Hello world " + new Date();
        Files.writeString(new File(contentRoot,"test.txt").toPath(), fileContent);

        HttpClient client = new HttpClient("localhost", server.getPort(), "/test.txt");
        assertEquals(fileContent, client.getResponseBody());
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));;

    }

    @Test
    void shouldReturnCorrectContentType() throws IOException{
        File contentRoot = new File("target/test-classes");

        Files.writeString(new File(contentRoot,"index.html").toPath(), "<h2>Hello World</h2>");

        HttpClient client = new HttpClient("localhost", server.getPort(), "/index.html");
        assertEquals("text/html", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldReturn404IfFileNotFound() throws IOException{
        File contentRoot = new File("target/test-classes");

        HttpClient client = new HttpClient("localhost", server.getPort(), "/notFound.txt");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldPostNewworker() throws IOException, SQLException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/workers", "POST", "worker_name=Carlo&email_address=carlo@gmail.com");
        assertEquals(302, client.getStatusCode());
        assertThat(server.getWorkers())
                .extracting(Worker::getName)
                .contains("Carlo");
    }

    @Test
    void shouldReturnExistingworker() throws IOException, SQLException {
        WorkerDao workerDao = new WorkerDao(dataSource);
        Worker worker = new Worker();
        worker.setName("Chris");
        worker.setEmail("haha@gmail.com");
        workerDao.insert(worker);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/projectworkers");
        assertThat(client.getResponseBody()).contains("<li>Name: Chris<br> Email Address: haha@gmail.com</li>");
    }

    @Test
    void shouldPostNewProject() throws IOException, SQLException {
        String requestBody = "name=HousingProject";
        HttpClient postClient = new HttpClient("localhost", server.getPort(), "/api/newProject", "POST", requestBody);
        assertEquals(302, postClient.getStatusCode());

        HttpClient getClient = new HttpClient("localhost", server.getPort(), "/api/projects");
        assertThat(getClient.getResponseBody()).contains("<li>HousingProject</li>");
    }

}