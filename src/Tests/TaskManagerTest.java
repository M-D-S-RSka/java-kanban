package Tests;

import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;
import static tasks.Status.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Приветствую! Была мысль написать что поправил, но надолго растянулось выполнение ( сломал код, весь перелопатил
// дебагером, в итоге решил многое написать заново/исправить). Поэтому извиняюсь, если будут сложности с проверкой из-за
// этого. Хорошего времени суток)

public abstract class TaskManagerTest<T extends TaskManager> {
    T taskManager;
    protected Task task = createTask();
    protected Epic epic = createEpic();
    protected Subtask subtask = createSubtask();

    public abstract T createTaskManager();

    @BeforeEach
    public void BeforeEach() {
        taskManager = createTaskManager();
    }

    public Task createTask() {
        return new Task("Test createTask", "description", NEW);
    }

    public Subtask createSubtask() {
        return new Subtask("Test createSubtask", "description", NEW);
    }

    public Epic createEpic() {
        return new Epic("Test createEpic", "description", NEW);
    }

    @Test
    void addNewTask() {
        final int taskId = taskManager.createTask(task);
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи нe возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
        taskManager.deleteTaskById(taskId);
        assertFalse(taskManager.getTasks().contains(task), "Задачи не удаляются.");
    }

    @Test
    void addNewEpic() {
        final int epicId = taskManager.createEpic(epic);
        final Task savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");
        assertEquals(NEW, epic.getStatus(), "Статусы не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Задачи нe возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(0), "Задачи не совпадают.");
        taskManager.deleteEpicById(epicId);
        assertFalse(taskManager.getEpics().contains(epic), "Задачи не удаляются.");
    }

    @Test
    void addNewSubtask() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        final int subtaskId = taskManager.createSubtask(subtask);
        final int subtaskEpicId = taskManager.getSubtaskById(subtaskId).getEpicId();

        assertNotNull(taskManager.getEpicById(subtaskEpicId), "Epic не найден по subtaskId.");
        assertEquals(epicId, subtaskEpicId, "EpicId не совпадают.");
        assertEquals(epic, taskManager.getEpicById(subtaskEpicId), "Epics не совпадают.");

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Задачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void getHistory() {
        assertTrue(taskManager.getHistory().isEmpty(), "Имеется история.");

        final int taskId = taskManager.createTask(task);
        taskManager.getTaskById(taskId);

        assertFalse(taskManager.getHistory().isEmpty(), "Отсутствует история.");
        assertEquals(1, taskManager.getHistory().size(), "Неверное количество задач.");
    }

    @Test
    void getTasks() {
        assertTrue(taskManager.getTasks().isEmpty(), "Имеется история.");

        taskManager.createTask(task);

        assertTrue(taskManager.getTasks().contains(task), "Задачи не совпадают.");
        assertEquals(1, taskManager.getTasks().size(), "Неверное количество задач.");
    }

    @Test
    void getSubtasks() {
        assertTrue(taskManager.getSubtasks().isEmpty(), "Имеется история.");

        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);

        assertEquals(epic, taskManager.getEpicById(subtask.getEpicId()),
                "У сабтаски некорректный идентефикатор эпика.");
        assertTrue(taskManager.getSubtasks().contains(subtask), "Задачи не совпадают.");
        assertEquals(1, taskManager.getSubtasks().size(), "Неверное количество задач.");
    }

    @Test
    void getEpics() {
        assertTrue(taskManager.getEpics().isEmpty(), "Имеется история.");

        final int epicId = taskManager.createEpic(epic);
        assertEquals(0, taskManager.getEpicById(epicId).getSubtaskIds().size(),
                "Неверное количество подзадач.");
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);

        assertTrue(taskManager.getEpics().contains(epic), "Задачи не совпадают.");
        assertEquals(1, taskManager.getEpics().size(), "Неверное количество задач.");
    }

    @Test
    void updateTask() {
        taskManager.createTask(task);
        assertEquals(NEW, task.getStatus());

        task.setStatus(IN_PROGRESS);
        assertEquals(IN_PROGRESS, task.getStatus());

        task.setStatus(DONE);
        assertEquals(DONE, task.getStatus());
    }

    @Test
    void updateSubtask() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);

        taskManager.createSubtask(subtask);
        assertEquals(NEW, subtask.getStatus());

        subtask.setStatus(IN_PROGRESS);
        assertEquals(IN_PROGRESS, subtask.getStatus());

        subtask.setStatus(DONE);
        assertEquals(DONE, subtask.getStatus());
    }

    @Test
    void updateEpic() {
        final int epicId = taskManager.createEpic(epic);
        assertEquals(NEW, epic.getStatus(), "Статус не совпадает.");

        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask("subtask #2", "description2", NEW);
        subtask2.setEpicId(epicId);
        taskManager.createSubtask(subtask2);
        assertEquals(NEW, epic.getStatus(), "Статус не совпадает.");

        subtask.setName("Subtask #1");
        subtask.setDescription("description1");
        subtask.setStatus(DONE);
        taskManager.updateSubtask(subtask);
        assertEquals(IN_PROGRESS, epic.getStatus(), "Статус не совпадает.");

        subtask2.setName("Subtask #2");
        subtask2.setDescription("description2");
        subtask2.setStatus(DONE);
        taskManager.updateSubtask(subtask2);
        assertEquals(DONE, epic.getStatus(), "Статус не совпадает.");

        subtask.setStatus(IN_PROGRESS);
        subtask2.setStatus(IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask2);
        assertEquals(IN_PROGRESS, epic.getStatus(), "Статус не совпадает.");
    }

    @Test
    void getTask() {
        final int taskId = taskManager.createTask(task);
        assertEquals(1, taskId, "Id задач не совпадают.");
    }

    @Test
    void getSubtask() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        final int subtaskId = taskManager.createSubtask(subtask);
        assertEquals(2, subtaskId, "Id задач не совпадают.");
    }

    @Test
    void getEpic() {
        final int epicId = taskManager.createEpic(epic);
        assertEquals(1, epicId, "Id задач не совпадают.");
    }

    @Test
    void deleteAllTask() {
        taskManager.createTask(task);
        taskManager.deleteAllTask();
        assertEquals(0, taskManager.getTasks().size(), "Задачи не удаляются.");
    }

    @Test
    void deleteAllSubtask() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);
        taskManager.deleteAllSubtask();

        assertEquals(0, taskManager.getSubtasks().size(), "Сабтаски не удаляются.");
        assertEquals(0, taskManager.getEpicById(epicId).getSubtaskIds().size(),
                "Сабтаски Epica не удаляются.");
    }

    @Test
    void deleteAllEpic() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);
        taskManager.deleteAllEpic();

        assertEquals(0, taskManager.getEpics().size(), "Epic задачи не удаляются.");
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи Epic не удаляются.");
    }

    @Test
    void deleteTask() {
        final int taskId = taskManager.createTask(task);
        taskManager.deleteTaskById(taskId);
        assertEquals(0, taskManager.getTasks().size(), "Задачи не удаляются.");
    }

    @Test
    void deleteSubtask() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        final int subtaskId = taskManager.createSubtask(subtask);
        taskManager.deleteSubtaskById(subtaskId);

        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи не удаляются.");
        assertEquals(0, taskManager.getEpicById(epicId).getSubtaskIds().size(),
                "Подзадачи Epica не удаляются.");
    }

    @Test
    void deleteEpic() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);
        taskManager.deleteEpicById(epicId);

        assertEquals(0, taskManager.getEpics().size(), "Epic задачи не удаляются.");
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи Epic не удаляются.");
    }

    @Test
    void getEpicSubtasks() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        final int subtaskId = taskManager.createSubtask(subtask);
        Subtask subtaskEpic = taskManager.getSubtaskById(taskManager.getEpicById(epicId).getSubtaskIds().get(0));

        assertEquals(taskManager.getSubtaskById(subtaskId), subtaskEpic, "Сабтаски не совпадают.");
    }

    @Test
    void getPrioritizedTasks() {
        final int epicId = taskManager.createEpic(epic);
        subtask.setEpicId(epicId);
        subtask.setStartTime(LocalDateTime.of(2024,1,10,15,0));
        subtask.setDuration(Duration.ofMinutes(15));

        Task task = createTask();
        final int taskId = taskManager.createTask(task);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(25));

        assertEquals(LocalDateTime.of(2024,1,10,15,0), subtask.getStartTime());
        assertTrue(taskManager.getPrioritizedTasks().contains(taskManager.getTaskById(taskId)));
    }
}