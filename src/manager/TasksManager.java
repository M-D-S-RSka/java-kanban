package manager;

import tasks.Epic;
import tasks.StatusOfTask;
import tasks.Subtask;
import tasks.Task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TasksManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer generatedTaskId = 1;

    private int generateId() {
        return generatedTaskId++;
    }

    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        if (task.getId() != null) {
            return;
        }
        task.setId(generateId());
        if (tasks.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        tasks.put(task.getId(), task);
    }

    public void createSubtask(Subtask task) {
        int epicId;
        if (task == null) {
            return;
        }
        if (task.getId() != null) {
            return;
        }
        task.setId(generateId());
        if (subtasks.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        epicId = task.getEpicId();
        if (epics.containsKey(epicId)) {
            subtasks.put(task.getId(), task);
            epics.get(epicId).addSubtaskId(task);
            updateEpicStatus(epicId);
        }
    }

    public void createEpic(Epic task) {
        if (task == null) {
            return;
        }
        if (task.getId() != null) {
            return;
        }
        task.setId(generateId());
        if (epics.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        epics.put(task.getId(), task);
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void updateTask(Task task) {
        if (task == null)
            return;
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        final Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    public void updateSubtask(Subtask task) {
        if (task == null)
            return;
        if (subtasks.containsKey(task.getId())) {
            subtasks.put(task.getId(), task);
            epics.get(task.getEpicId()).addSubtaskId(task);
            updateEpicStatus(task.getEpicId());
        }
    }

    public List<Subtask> getAllEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtaskIds();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setStatusOfTask(StatusOfTask.NEW);
            return;
        }
        StatusOfTask status = null;
        for (Subtask task : subs) {
            final Subtask subtask = subtasks.get(task.getId());
            if (status == null) {
                status = subtask.getStatusOfTask();
                continue;
            }
            if (status == subtask.getStatusOfTask()
                    && status != StatusOfTask.IN_PROGRESS) {
                continue;
            }
            epic.setStatusOfTask(StatusOfTask.IN_PROGRESS);
            return;
        }
        epic.setStatusOfTask(status);
    }

    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
    }

    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Subtask subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            int epicId = subtasks.get(id).getEpicId();
            subtasks.remove(id);
            epics.get(epicId).removeSubTask(id);
            updateEpicStatus(epicId);
        }
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        return null;
    }

    public Epic getEpicById(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        return null;
    }

    public Subtask getSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        return null;
    }

    public void deleteAllTask() {
        tasks.clear();
    }

    public void deleteAllEpic() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtask() {
        subtasks.clear();
        if (!epics.isEmpty()) {
            for (Epic epic : epics.values()) {
                epic.clearSubtask();
                updateEpicStatus(epic.getId());
            }
        }
    }
}