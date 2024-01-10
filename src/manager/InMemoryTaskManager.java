package manager;

import tasks.*;

import java.util.*;

import static tasks.Status.IN_PROGRESS;
import static tasks.Status.NEW;


public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> treeSet = new TreeSet<>();
    protected Integer generatedTaskId = 0;

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        treeSet.clear();
        treeSet.addAll(tasks.values());
        treeSet.addAll(subtasks.values());
        return treeSet;
    }

    @Override
    public Integer createTask(Task task) {
        final int id = ++generatedTaskId;
        task.setId(id);

        if (task.getStartTime() != null) {
            if (!getBooleanPrioritizedTasks(task)) {
                System.out.println("Задача не создана, данное время занято.");
                return --generatedTaskId;
            }
        }
        tasks.put(id, task);
        return id;
    }


    @Override
    public Integer createSubtask(Subtask task) {
        final int id = ++generatedTaskId;

        if (task.getStartTime() != null) {
            if (!getBooleanPrioritizedTasks(task)) {
                System.out.println("Задача не создана, данное время занято.");
                return --generatedTaskId;
            }
        }

        task.setId(id);
        int epicId = task.getEpicId();
        Epic epic = epics.get(epicId);
        epic.addSubtaskIds(id);
        subtasks.put(id, task);

        if (task.getStartTime() != null) {
            if (epic.getSubtaskIds().size() == 1) {
                epic.setDuration(task.getDuration());
                epic.setStartTime(task.getStartTime());
                epic.setEndTime(task.getEndTime());
            } else {
                updateTimeEpic(epic);
            }
        }
        return id;
    }

    @Override
    public Integer createEpic(Epic task) {
        final int id = ++generatedTaskId;
        task.setId(id);
        epics.put(id, task);
        return id;
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
            if (task.getStartTime() == null) {
                tasks.put(task.getId(), task);
            } else if (getBooleanPrioritizedTasks(task)) {
                tasks.put(task.getId(), task);
            } else {
                throw new RuntimeException("Время начала задачи уже занято.");
            }
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
            if (task.getStartTime() == null) {
                subtasks.put(task.getId(), task);
                epics.get(task.getEpicId()).addSubtaskIds(task.getId());
                updateEpicStatus(task.getEpicId());
            } else if (subtasks.get(task.getId()).getStartTime().equals(task.getStartTime())) {
                subtasks.put(task.getId(), task);
                epics.get(task.getEpicId()).addSubtaskIds(task.getId());
                updateEpicStatus(task.getEpicId());
                updateTimeEpic(epics.get(task.getEpicId()));
            } else if (!getBooleanPrioritizedTasks(task)) {
                throw new RuntimeException("Время начала задачи уже занято.");
            } else {
                subtasks.put(task.getId(), task);
                updateEpicStatus(task.getEpicId());
                updateTimeEpic(epics.get(task.getEpicId()));
            }
        }
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int epicId) {
        ArrayList<Subtask> subtaskArrayList = new ArrayList<>();
        Epic epic = epics.get(epicId);

        for (Integer idSubtask : epic.getSubtaskIds()) {
            subtaskArrayList.add(subtasks.get(idSubtask));
        }
        return subtaskArrayList;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        Status status = null;
        if (subs.isEmpty()) {
            epic.setStatus(NEW);
            return;
        }
        for (Integer task : subs) {
            final Subtask subtask = subtasks.get(task);
            if (status == null) {
                status = subtask.getStatus();
                continue;
            }
            if (status == subtask.getStatus()
                    && status != IN_PROGRESS) {
                continue;
            }
            epic.setStatus(IN_PROGRESS);
            return;
        }
        epic.setStatus(status);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Integer epicId = subtasks.get(id).getEpicId();
            Epic epic = epics.get(epicId);
            updateTimeEpic(epic);
            subtasks.remove(id);
            epics.get(epicId).removeSubtask(id);
            historyManager.remove(id);
            updateEpicStatus(epicId);
            updateTimeEpic(epic);
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
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpic() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        epics.clear();
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtask() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtask();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public Boolean getBooleanPrioritizedTasks(Task task) {
        boolean isTask = true;
        for (Task taskPrioritized : getPrioritizedTasks()) {
            if (task.getStartTime().equals(taskPrioritized.getStartTime())
                    || task.getStartTime().equals(taskPrioritized.getEndTime())
                    || task.getEndTime().equals(taskPrioritized.getEndTime())) {
                isTask = false;
            }
        }
        return isTask;
    }

    private void updateTimeEpic(Epic epic) {
        if (epic.getStartTime() != null) {
            for (Integer idSubtask : epic.getSubtaskIds()) {
                if (epic.getStartTime().isAfter(subtasks.get(idSubtask).getStartTime())) {
                    epic.setDuration(subtasks.get(idSubtask).getDuration());
                    epic.setStartTime(subtasks.get(idSubtask).getStartTime());
                }
                if (epic.getEndTime().isBefore(subtasks.get(idSubtask).getEndTime())) {
                    epic.setEndTime(subtasks.get(idSubtask).getEndTime());
                }
            }
        }
    }
}