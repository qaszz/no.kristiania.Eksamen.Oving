package no.kristiania.http;

import no.kristiania.database.ProjectDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateProjectController implements HttpController{
    public UpdateProjectController(ProjectDao projectDao) {
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {

    }
}
