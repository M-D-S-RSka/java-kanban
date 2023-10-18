package manager;

import tasks.Epic;
import tasks.StatusOfTask;
import tasks.Subtask;
import tasks.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class TasksManager {
    private final HashMap<Integer, Task> taskList = new HashMap<>();
    private final HashMap<Integer, Epic> epicList = new HashMap<>();
    private final HashMap<Integer, Subtask> subtaskList = new HashMap<>();

    private Integer generatedTaskId = 1;

    private int generateId() {
        return generatedTaskId++;
    }

    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        if (task.getTaskId() != null) {
            return;
        }
        task.setTaskId(generateId());
        if (taskList.containsKey(task.getTaskId())) {
            return;
        }
        taskList.put(task.getTaskId(), task);
    }

    public void createSubtask(Subtask task) {
        int epicId;
        if (task == null) {
            return;
        }
        if (task.getTaskId() != null) {
            return;
        }
        task.setTaskId(generateId());
        if (subtaskList.containsKey(task.getTaskId())) {
            return;
        }
        epicId = task.getEpicId();
        if (epicList.containsKey(epicId)) {
            subtaskList.put(task.getTaskId(), task);
            epicList.get(epicId).checkSubtask(task);
            updateEpicStatus(epicId);
        }
    }

    public void createEpic(Epic task) {
        if (task == null) {
            return;
        }
        if (task.getTaskId() != null) {
            return;
        }
        task.setTaskId(generateId());
        if (epicList.containsKey(task.getTaskId())) {
            return;
        }
        epicList.put(task.getTaskId(), task);
    }

    public ArrayList<Task> getAllTask() {
        ArrayList<Task> tasksList = new ArrayList<>();
        if (!taskList.isEmpty()) {
            for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
                tasksList.add(entry.getValue());
            }
        }
        return tasksList;
    }

    public ArrayList<Epic> getAllEpic() {
        ArrayList<Epic> tasksList = new ArrayList<>();
        if (!epicList.isEmpty()) {
            for (Map.Entry<Integer, Epic> entry : epicList.entrySet()) {
                tasksList.add(entry.getValue());
            }
        }
        return tasksList;
    }

    public ArrayList<Subtask> getAllSubTask() {
        ArrayList<Subtask> tasksList = new ArrayList<>();
        if (!subtaskList.isEmpty()) {
            for (Map.Entry<Integer, Subtask> entry : subtaskList.entrySet()) {
                tasksList.add(entry.getValue());
            }
        }
        return tasksList;
    }

    public HashMap<Integer, Object> getAllTaskAllTypes() {
        HashMap<Integer, Object> allTasksList = new HashMap<>();
        if (!taskList.isEmpty()) {
            allTasksList.putAll(taskList);
        }
        if (!epicList.isEmpty()) {
            allTasksList.putAll(epicList);
        }
        if (!subtaskList.isEmpty()) {
            allTasksList.putAll(subtaskList);
        }
        return allTasksList;
    }

    public void updateTask(Task task) {
        if (task == null)
            return;
        if (taskList.containsKey(task.getTaskId())) {
            taskList.put(task.getTaskId(), task);
        }
    }

    public void updateEpic(Epic task) {
        if (task == null)
            return;
        if (epicList.containsKey(task.getTaskId())) {
            epicList.put(task.getTaskId(), task);
            updateEpicStatus(task.getTaskId());
        }
    }

    public void updateSubtask(Subtask task) {
        if (task == null)
            return;
        if (subtaskList.containsKey(task.getTaskId())) {
            subtaskList.put(task.getTaskId(), task);
            epicList.get(task.getEpicId()).checkSubtask(task);
            updateEpicStatus(task.getEpicId());
        }
    }

    public ArrayList<Subtask> getAllEpicSubTasks(int epicId) {
        ArrayList<Subtask> listOfSubtask = new ArrayList<>();
        if (epicId > 0) {
            if (!subtaskList.isEmpty()) {
                for (Map.Entry<Integer, Subtask> entry : subtaskList.entrySet()) {
                    if (entry.getValue().getEpicId() == epicId) {
                        listOfSubtask.add(entry.getValue());
                    }
                }
            }
        }
        return listOfSubtask;
    }

    private void updateEpicStatus(int Id) {
        if (epicList.containsKey(Id)) {
            Epic currentEpic = epicList.get(Id);
            ArrayList<Integer> subtasksList = currentEpic.getSubtaskList();
            int newTask = 0;
            int inProgress = 0;
            int doneTask = 0;
            if (!subtaskList.isEmpty()) {
                for (Integer taskNum : subtasksList) {
                    if (subtaskList.get(taskNum).getStatusOfTask().equals("DONE")) {
                        doneTask++;
                    }
                    if (subtaskList.get(taskNum).getStatusOfTask().equals("NEW")) {
                        newTask++;
                    }
                    if (subtaskList.get(taskNum).getStatusOfTask().equals("IN_PROGRESS")) {
                        inProgress++;
                    }
                }
            } else {
                currentEpic.setStatusOfTask(StatusOfTask.valueOf("NEW"));
            }
            if ((newTask > 0) && (inProgress == 0) && (doneTask == 0)) {
                currentEpic.setStatusOfTask(StatusOfTask.valueOf("NEW"));
                return;
            }
            if (inProgress > 0) {
                currentEpic.setStatusOfTask(StatusOfTask.valueOf("IN_PROGRESS"));
                return;
            }
            if ((doneTask > 0) && (inProgress == 0) && (newTask == 0)) {
                currentEpic.setStatusOfTask(StatusOfTask.valueOf("DONE"));
                return;
            }
            if ((doneTask > 0) && (inProgress == 0) && (newTask > 0)) {
                currentEpic.setStatusOfTask(StatusOfTask.valueOf("IN_PROGRESS"));
                return;
            }
            currentEpic.setStatusOfTask(StatusOfTask.valueOf("NEW"));
        }
    }

    public void deleteTaskById(int taskId) {
        if (taskList.containsKey(taskId)) {
            taskList.remove(taskId);
        } else if (epicList.containsKey(taskId)) {
            ArrayList<Integer> subsList = epicList.get(taskId).getSubtaskList();
            if (!subsList.isEmpty()) {
                for (Integer entry : subsList) {
                    subtaskList.remove(entry);
                }
            }
            epicList.remove(taskId);
        } else if (subtaskList.containsKey(taskId)) {
            int epicId = subtaskList.get(taskId).getEpicId();
            subtaskList.remove(taskId);
            epicList.get(epicId).removeSubTask(taskId);
            updateEpicStatus(epicId);
        }
    }

    public Object getAnyTaskById(int Id) {
        Object object = new Object();
        if (!taskList.containsKey(Id)) {
            if (!subtaskList.containsKey(Id)) {
                if (!epicList.containsKey(Id)) {
                    return object;
                } else {
                    return epicList.get(Id);
                }
            } else {
                return subtaskList.get(Id);
            }
        } else {
            return taskList.get(Id);
        }
    }

    public Task getTaskById(int Id) {
        return taskList.getOrDefault(Id, null);
    }

    public Epic getEpicById(int Id) {
        return epicList.getOrDefault(Id, null);
    }

    public Subtask getSubtaskById(int Id) {
        return subtaskList.getOrDefault(Id, null);
    }

    public void deleteAllTask() {
        taskList.clear();
    }

    public void deleteAllEpic() {
        epicList.clear();
        subtaskList.clear();
    }

    public void deleteAllSubtask() {
        subtaskList.clear();
        if (!epicList.isEmpty()) {
            for (Map.Entry<Integer, Epic> entry : epicList.entrySet()) {
                entry.getValue().clearSubtask();
                updateEpicStatus(entry.getValue().getTaskId());
            }
        }
    }
}