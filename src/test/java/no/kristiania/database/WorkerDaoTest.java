package no.kristiania.database;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
class WorkerDaoTest {

    @Test
    void shouldListInsertedWorkers() {
        WorkerDao workerDao = new WorkerDao();
        String worker = exampleWorkerName();
        workerDao.insert(worker);
        assertThat(workerDao.list()).contains(worker);
    }

    private String exampleWorkerName() {
        String[] options = { "Ole", "Ali", "Chris", "Bjørn"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }
}