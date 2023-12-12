package manager;

import exception.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import static tasks.TaskStatus.*;
import static tasks.Type.*;


public class FileBackedTasksManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("name,details,id,status,type,epicId" + "\n");
            for (Task task : getAllTasks()) {
                writer.write(toStringTask(task) + "\n");
            }
            writer.write("\n" + historyToString(historyManager));
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
    }

    public String toStringTask(Task task) {
        String result = new String();
        result = result + task.getName() + "," + task.getDescription() + "," + task.getId() + "," + task.getStatus() + "," + task.getType();
        if (task.getType() == SUBTASK) {
            result = result + "," + ((Subtask) task).getEpicId();
        }
        return result;
    }

    public Task fromString(String value) {
        String[] taskArray = value.split(",");
        String name = taskArray[0];
        String description = taskArray[1];
        int id = Integer.parseInt(taskArray[2]);
        TaskStatus status = TaskStatus.valueOf(taskArray[3]);
        Type type = Type.valueOf(taskArray[4]);
        Task task = null;
        int epicId;
        if (type == TASK) {
            task = new Task(name, description, status, id, type);
        } else if ((type == EPIC)) {
            task = new Epic(name, description, status, id, type);
        } else if (type == SUBTASK) {
            epicId = Integer.parseInt(taskArray[5]);
            task = new Subtask(name, description, status, epicId, id, type);
        }
        return task;
    }

    static String historyToString(HistoryManager manager) {
        String history;
        List<String> tasks = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            tasks.add(Integer.toString(task.getId()));
        }
        history = String.join(",", tasks);
        return history;
    }

    static List<Integer> historyFromString(String value) {
        String[] split = value.split(",");
        List<Integer> newList = new ArrayList<>();
        for (String str : split) {
            newList.add(Integer.parseInt(str));
        }
        return newList;
    }

    static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int counter = 1;
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.equals("name,details,id,status,type,epicId")) {
                    continue;
                }
                if (line.isBlank()) {
                    break;
                } else {
                    Task task = fileBackedTasksManager.fromString(line);
                    counter++;
                    if (task.getType() == TASK) {
                        fileBackedTasksManager.tasks.put(task.getId(), task);
                    } else if (task.getType() == EPIC) {
                        fileBackedTasksManager.epics.put(task.getId(), (Epic) task);

                    } else if (task.getType() == SUBTASK) {
                        fileBackedTasksManager.subtasks.put(task.getId(), (Subtask) task);
                        Epic epic = fileBackedTasksManager.epics.get(((Subtask) task).getEpicId());
                        List<Integer> list = epic.getSubtaskIds();
                        list.add(task.getId());
                    }
                }
            }
            fileBackedTasksManager.generatedTaskId = counter;
            String history = bufferedReader.readLine();
            for (Integer id : historyFromString(history)) {
                if (fileBackedTasksManager.tasks.containsKey(id)) {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.tasks.get(id));
                }
                if (fileBackedTasksManager.subtasks.containsKey(id)) {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.subtasks.get(id));
                }
                if (fileBackedTasksManager.epics.containsKey(id)) {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.epics.get(id));
                }
            }
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
        return fileBackedTasksManager;
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            allTasks.add(task);
        }
        for (Task task : epics.values()) {
            allTasks.add(task);
        }
        for (Task task : subtasks.values()) {
            allTasks.add(task);
        }
        return allTasks;
    }

    public static void main(String[] args) {
        File file = new File("file.csv");
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);

        Task task1 = new Task("Task #1", "Task1 description", NEW, TASK);
        fileBackedTasksManager.createTask(task1);
        Task task2 = new Task("Task #2", "Task2 description", IN_PROGRESS, TASK);
        fileBackedTasksManager.createTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description", NEW, EPIC);
        fileBackedTasksManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic #2", "Epic2 description", NEW, EPIC);
        fileBackedTasksManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1", "Subtask1 description", NEW, epic1.getId(), SUBTASK);
        fileBackedTasksManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask #2", "Subtask2 description", NEW, epic1.getId(), SUBTASK);
        fileBackedTasksManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask #3", "Subtask3 description", DONE, epic1.getId(), SUBTASK);
        fileBackedTasksManager.createSubtask(subtask3);

        fileBackedTasksManager.getTasks();
        fileBackedTasksManager.getSubtaskById(5);

        FileBackedTasksManager fileBackedTasksManager2 = loadFromFile(file);
        Epic epic3 = new Epic("Epic #3", "Epic3 description", NEW, EPIC);
        fileBackedTasksManager2.createEpic(epic3);
    }

    @Override
    public Integer createTask(Task task) {
        super.createTask(task);
        save();
        return task.getId();
    }

    @Override
    public Integer createSubtask(Subtask task) {
        super.createSubtask(task);
        save();
        return task.getId();
    }

    @Override
    public Integer createEpic(Epic task) {
        super.createEpic(task);
        save();
        return task.getId();
    }

    @Override
    public List<Task> getTasks() {
        List<Task> tasks = super.getTasks();
        save();
        return tasks;
    }

    public List<Epic> getEpics() {
        List<Epic> tasks = super.getEpics();
        save();
        return tasks;
    }

    @Override
    public List<Subtask> getSubtasks() {
        List<Subtask> tasks = super.getSubtasks();
        save();
        return tasks;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(Epic epic) {
        List<Subtask> subtasks = super.getAllEpicSubtasks(epic);
        save();
        return subtasks;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task foundTask = super.getTaskById(id);
        if (foundTask != null) {
            save();
            return foundTask;
        } else {
            return null;
        }
    }

    @Override
    public Epic getEpicById(int id) {
        Epic foundEpic = super.getEpicById(id);
        if (foundEpic != null) {
            save();
            return foundEpic;
        } else {
            return null;
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask foundSubtask = super.getSubtaskById(id);
        if (foundSubtask != null) {
            save();
            return foundSubtask;
        } else {
            return null;
        }
    }

    @Override
    public void deleteAllTask() {
        super.deleteAllTask();
        save();
    }

    @Override
    public void deleteAllEpic() {
        super.deleteAllEpic();
        save();
    }

    @Override
    public void deleteAllSubtask() {
        super.deleteAllSubtask();
        save();
    }
}