import org.junit.jupiter.api.Test;
import manager.TaskManager;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static utilities.Status.*;
import static utilities.Type.*;

class EpicTest {
    TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test createTask", "description", NEW);
        final int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addNewEpicAndCheckStatusINPROGRESS() {
        Epic epic = new Epic("Test createTask", "description", IN_PROGRESS);
        final int taskId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("subtask #1", "description1", NEW,
                Duration.ofMinutes(15),LocalDateTime.now().plusHours(1), taskId);
        Subtask subtask2 = new Subtask("subtask #2", "description2", IN_PROGRESS,
                Duration.ofMinutes(15),LocalDateTime.now().plusHours(3), taskId);
        Subtask subtask3 = new Subtask("subtask #3", "description3", DONE,
                Duration.ofMinutes(15),LocalDateTime.now(), taskId);

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        assertEquals(IN_PROGRESS, epic.getStatus());
    }

    @Test
    void addNewEpicAndCheckStatusNEW() {
        Epic epic2 = new Epic("Test createTask", "description", NEW);
        final int taskId2 = taskManager.createEpic(epic2);

        Subtask subtask4 = new Subtask("subtask #4", "description4", NEW,
                Duration.ofMinutes(15),LocalDateTime.now().plusMinutes(15), taskId2);
        Subtask subtask6 = new Subtask("subtask #6", "description6", IN_PROGRESS,
                Duration.ofMinutes(15),LocalDateTime.now(), taskId2);
        taskManager.createSubtask(subtask4);
        taskManager.createSubtask(subtask6);
        assertEquals(NEW, epic2.getStatus());
        assertEquals(EPIC, epic2.getType());
    }

    @Test
    void addNewEpicAndCheckTypeEPIC() {
        Epic epic2 = new Epic("Test createTask", "description", NEW);
        taskManager.createEpic(epic2);
        assertEquals(EPIC, epic2.getType());
    }

    @Test
    void addNewEpicAndCheckStatusDone() {
        Epic epic2 = new Epic("Test createTask", "description", DONE);
        final int taskId2 = taskManager.createEpic(epic2);

        Subtask subtask4 = new Subtask("subtask #4", "description4", DONE,
                Duration.ofMinutes(1),LocalDateTime.now(), taskId2);
        Subtask subtask6 = new Subtask("subtask #6", "description6", DONE,
                Duration.ofDays(60),LocalDateTime.now().plusMinutes(20), taskId2);
        taskManager.createSubtask(subtask4);
        taskManager.createSubtask(subtask6);
        assertEquals(DONE, epic2.getStatus());
        assertEquals(EPIC, epic2.getType());
        assertEquals(SUBTASK, subtask4.getType());
    }
}