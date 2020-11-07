package no.kristiania.http;

import no.kristiania.database.Project;
import no.kristiania.database.ProjectDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ProjectPostController implements HttpController{
    private ProjectDao projectDao;

    public ProjectPostController(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }
    public HttpMessage handle(HttpMessage request) throws SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Project project = new Project();
        project.setName(requestParameter.getParameter("name"));
        projectDao.insert(project);

        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 Redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/projects.html");
        return redirect;
    }
    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {

        HttpMessage response = handle(request);
        response.write(clientSocket);
    }
}
