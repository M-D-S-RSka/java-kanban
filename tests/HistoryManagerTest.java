import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static utilities.Status.NEW;

class HistoryManagerTest {
    HistoryManager historyManager;
    TaskManager taskManager;

    @BeforeEach
    public void createHistoryManager() {
        historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager();
    }

    public Task createTask() {
        return new Task("Test createTask", "description", NEW);
    }

    public Epic createEpic() {
        return new Epic("Test createEpic", "description", NEW);
    }

    @Test
    void add() {
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "История не пустая.");
        Task task = createTask();

        historyManager.addTask(task);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");

        historyManager.addTask(task);
        assertEquals(1, history.size(), "История дублируется.");
    }

    @Test
    void remove() {
        Task task = createTask();
        final int taskId = taskManager.createTask(task);
        Epic epic = createEpic();
        final int epicId = taskManager.createEpic(epic);

        historyManager.addTask(task);
        historyManager.addTask(epic);

        historyManager.remove(taskId);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задачи не удаляются из истории.");
        historyManager.remove(epicId);
        history = historyManager.getHistory();
        assertEquals(0, history.size(), "История не пустая после удаления.");
    }
}