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
        HttpMessage response = new HttpMessage(getBody());
        response.write(clientSocket);
    }

    public String getBody() throws SQLException {
        String body ="";
        for (Project project : projectDao.list()) {
            body +="<option value=" + Project.getId() + ">" + project.getName() +"</option>";
        }

        return body;
    }
}
