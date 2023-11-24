package manager;

import tasks.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer generatedTaskId = 1;

    private int generateId() {
        return generatedTaskId++;
    }

    @Override
    public Integer createTask(Task task) {
        if (task == null) {
            return null;
        }
        if (task.getId() != null) {
            return null;
        }
        task.setId(generateId());
        if (tasks.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public Integer createSubtask(Subtask task) {
        if (task == null) {
            return null;
        }
        if (task.getId() != null) {
            return null;
        }
        task.setId(generateId());
        if (subtasks.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        int epicId = task.getEpicId();
        if (epics.containsKey(epicId)) {
            subtasks.put(task.getId(), task);
            epics.get(epicId).addSubtaskId(task);
            updateEpicStatus(epicId);
        }
        return task.getId();
    }

    @Override
    public Integer createEpic(Epic task) {
        if (task == null) {
            return null;
        }
        if (task.getId() != null) {
            return null;
        }
        task.setId(generateId());
        if (epics.containsKey(task.getId())) {
            throw new RuntimeException("Задача уже существует с id" + task.getId());
        }
        epics.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void updateTask(Task task) {
        if (task == null)
            return;
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask task) {
        if (task == null)
            return;
        if (subtasks.containsKey(task.getId())) {
            subtasks.put(task.getId(), task);
            epics.get(task.getEpicId()).addSubtaskId(task);
            updateEpicStatus(task.getEpicId());
        }
    }

    @Override
    public List<Integer> getAllEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtaskIds();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        TaskStatus status = null;
        for (Integer task : subs) {
            final Subtask subtask = subtasks.get(task);
            if (status == null) {
                status = subtask.getStatus();
                continue;
            }
            if (status == subtask.getStatus()
                    && status != TaskStatus.IN_PROGRESS) {
                continue;
            }
            epic.setStatus(TaskStatus.IN_PROGRESS);
            return;
        }
        epic.setStatus(status);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            int epicId = subtasks.get(id).getEpicId();
            subtasks.remove(id);
            epics.get(epicId).removeSubtask(id);
            updateEpicStatus(epicId);
        }
    }

    @Override
    public Task getTaskById(int id) {
        final Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        final Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    @Override
    public void deleteAllTask() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpic() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtask() {
        subtasks.clear();
        if (!epics.isEmpty()) {
            for (Epic epic : epics.values()) {
                epic.clearSubtask();
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}