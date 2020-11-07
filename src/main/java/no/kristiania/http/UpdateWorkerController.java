package no.kristiania.http;

import no.kristiania.database.Worker;
import no.kristiania.database.WorkerDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateWorkerController implements HttpController{
    private final WorkerDao workerDao;

    public UpdateWorkerController(WorkerDao workerDao) {
        this.workerDao = workerDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Long workerId = Long.valueOf(requestParameter.getParameter("workerId"));
        Long projectId = Long.valueOf(requestParameter.getParameter("projectId"));
        Worker worker = workerDao.retrieve(workerId);
        worker.setId(workerId);

        workerDao.update(worker);

    }
}
