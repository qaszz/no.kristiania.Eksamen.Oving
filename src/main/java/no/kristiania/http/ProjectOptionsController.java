package no.kristiania.http;

import no.kristiania.database.Project;
import no.kristiania.database.ProjectDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ProjectOptionsController implements HttpController{
    private ProjectDao projectDao;

    public ProjectOptionsController(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        String body = getBody();
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }

    public String getBody() throws SQLException {
        String body ="";
        for (Project project : projectDao.list()) {
            body +="<option value=" + project.getId() + ">" + project.getName() +"</option>";
        }

        return body;
    }
}
