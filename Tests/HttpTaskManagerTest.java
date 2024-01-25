import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.KVServer;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utilities.Status.NEW;

class HttpTaskManagerTest {
    private static KVServer server;
    private static TaskManager manager;

    @BeforeEach
    public void createManager() {
        manager.deleteAllTask();
        manager.deleteAllEpic();
        manager.deleteAllSubtask();
    }

    @BeforeAll
    static void startServer() throws IOException {
        server = new KVServer();
        server.start();
        try {
            manager = Managers.getDefault("http://localhost:8078");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void shouldLoadTasks() {
        Task task1 = new Task("name1", "description1", NEW);
        Task task2 = new Task("name2", "description2", NEW);
        int id1 = manager.createTask(task1);
        int id2 = manager.createTask(task2);
        manager.getTaskById(id1);
        manager.getTaskById(id2);
        List<Task> list = manager.getHistory();
        assertEquals(manager.getTasks(), list);
    }

    @Test
    public void shouldLoadEpics() {
        Epic epic1 = new Epic("name1", "description1", NEW);
        Epic epic2 = new Epic("name2", "description2", NEW);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        assertEquals(epic1, manager.getEpicById(epic1.getId()));
        assertEquals(epic2, manager.getEpicById(epic2.getId()));
        List<Task> list = manager.getHistory();
        assertEquals(manager.getEpics(), list);
    }

    @Test
    public void shouldLoadSubtasks() {
        Epic epic1 = new Epic("description1", "name1", NEW);
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("name1", "description1", NEW);
        subtask1.setEpicId(epic1.getId());
        Subtask subtask2 = new Subtask("name2", "description2", NEW);
        subtask2.setEpicId(epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        List<Task> list = manager.getHistory();
        assertEquals(manager.getSubtasks(), list);
    }

}