package manager;

import tasks.*;

import java.util.List;

public interface TaskManager {
    Integer createTask(Task task);

    Integer createSubtask(Subtask task);

    Integer createEpic(Epic task);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    List<Integer> getAllEpicSubtasks(int epicId);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void deleteAllTask();

    void deleteAllEpic();

    void deleteAllSubtask();

    List<Task> getHistory();
}