package manager;

import exception.ManagerSaveException;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import tasks.*;

import static tasks.Status.*;


public class FileBackedTasksManager extends InMemoryTaskManager {
    private static String path;

    public FileBackedTasksManager(String path) {
        FileBackedTasksManager.path = path;
    }

    protected void save() {
        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write("id,type,name,status,description,epic,duration,startTime,startTimeMinutes," +
                    "endTime,endTimeMinutes\n");
            for (Task task : tasks.values()) {
                String lineTask = toStringTask(task);
                writer.write(lineTask);
            }
            for (Task epic : epics.values()) {
                writer.write(toStringTask(epic));
            }
            for (Task subtask : subtasks.values()) {
                writer.write(toStringTask(subtask));
            }
            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
    }

    private String toStringTask(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        if (task.getStartTime() == null || task.getStartTime().isEqual(LocalDateTime.MAX)) {
            return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + ","
                    + task.getDescription() + "," + (task.getEpicId() == null ? "" : task.getEpicId()) + "\n";
        }
        return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + ","
                + task.getDescription() + "," + (task.getEpicId() == null ? "" : task.getEpicId()) + ","
                + (task.getDuration() == null ? "" : task.getDuration().getSeconds()/60) + ","
                + task.getStartTime().format(formatter) + "\n";
    }

    private static Task fromString(String value) {
        String[] taskArray = value.split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        int id = Integer.parseInt(taskArray[0]);
        String name = taskArray[2];
        Type type = Type.valueOf(taskArray[1]);
        Status status = Status.valueOf(taskArray[3]);
        String description = taskArray[4];
        LocalDateTime startTime = taskArray.length > 6 ? LocalDateTime.parse(taskArray[7], formatter) : LocalDateTime.MAX;
        Duration duration;
        if (startTime == null){
            duration = null;
        } else if (startTime.isEqual(LocalDateTime.MAX)) {
            duration = Duration.ofMinutes(0);
        } else {
            duration = Duration.ofMinutes(taskArray.length > 6 ? Integer.valueOf(taskArray[6]) : null);
        }
        switch (type) {
            case TASK:
                if (duration == null) {
                    return new Task(id, name, description, status);
                }
                return new Task(id, name, description, status, duration,
                        startTime);
            case EPIC:
                if (duration == null) {
                    return new Epic(id, name, description, status);
                }
                return new Epic(id, name, description, status, duration,
                        startTime);
            case SUBTASK:
                String epicIdString = taskArray[5];
                if (duration == null) {
                    return new Subtask(id, name, description, status, Integer.parseInt(epicIdString));
                }
                return new Subtask(id, name, description, status, Integer.parseInt(epicIdString),
                        duration, startTime);
            default:
                return null;
        }
    }

    public static String historyToString(HistoryManager manager) {
        StringBuilder history = new StringBuilder();
        List<Task> tasksHistory = manager.getHistory();

        for (int i = 0; i < tasksHistory.size(); i++) {
            if (i == tasksHistory.size() - 1) {
                history.append(tasksHistory.get(i).getId());
            } else {
                history.append(tasksHistory.get(i).getId()).append(",");
            }
        }
        return history.toString();
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> newList = new ArrayList<>();
        if (value != null) {
            String[] historyIdTasks = value.split(",");

            for (String id : historyIdTasks) {
                int idTask = Integer.parseInt(id);
                newList.add(idTask);
            }
        }
        return newList;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file.toString());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int counter = 0;

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.contains("id")) {
                    continue;
                }
                if (line.isEmpty()) {
                    break;
                }
                Task task = fromString(line);
                assert task != null;
                Type type = task.getType();
                Subtask subtask;
                Epic epic;

                switch (type) {
                    case SUBTASK:
                        subtask = (Subtask) task;
                        Integer epicId = subtask.getEpicId();
                        fileBackedTasksManager.subtasks.put(subtask.getId(), subtask);
                        fileBackedTasksManager.epics.get(epicId).addSubtaskIds(subtask.getId());
                        if (subtask.getId() > counter) {
                            counter = subtask.getId();
                        }
                        break;
                    case EPIC:
                        epic = (Epic) task;
                        fileBackedTasksManager.epics.put(epic.getId(), epic);
                        if (epic.getId() > counter) {
                            counter = epic.getId();
                        }
                        break;
                    case TASK:
                        fileBackedTasksManager.tasks.put(task.getId(), task);
                        if (task.getId() > counter) {
                            counter = task.getId();
                        }
                        break;
                }

            }
            fileBackedTasksManager.generatedTaskId = counter;
            String lineHistory = bufferedReader.readLine();
            for (Integer id : historyFromString(lineHistory)) {
                if (fileBackedTasksManager.epics.containsKey(id)) {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.epics.get(id));
                } else if (fileBackedTasksManager.subtasks.containsKey(id)) {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.subtasks.get(id));
                } else {
                    fileBackedTasksManager.historyManager.addTask(fileBackedTasksManager.tasks.get(id));
                }
            }
            return fileBackedTasksManager;
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
    }

    public static void main(String[] args) {
        File file = new File("file.csv");
        FileBackedTasksManager fileBackedTasksManager = getFileBackedTasksManager(file);

        fileBackedTasksManager.getTasks();
        fileBackedTasksManager.getSubtasks();

        FileBackedTasksManager fileBackedTasksManager2 = loadFromFile(file);

        System.out.println(fileBackedTasksManager.getTasks().equals(fileBackedTasksManager2.getTasks()));
        System.out.println(fileBackedTasksManager.getEpics().equals(fileBackedTasksManager2.getEpics()));
        System.out.println(fileBackedTasksManager.getSubtasks().equals(fileBackedTasksManager2.getSubtasks()));
        System.out.println(fileBackedTasksManager.historyManager.getHistory()
                .equals(fileBackedTasksManager2.historyManager.getHistory()));
    }

    private static FileBackedTasksManager getFileBackedTasksManager(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file.toString());

        Task task1 = new Task(1, "Task #1", "description1", NEW,
                Duration.ofMinutes(15), LocalDateTime.now().plusHours(5));
        fileBackedTasksManager.createTask(task1);
        Task task2 = new Task(2, "Task #2", "description2", IN_PROGRESS);
        fileBackedTasksManager.createTask(task2);

        Epic epic1 = new Epic(3, "Epic #1", "description1", NEW);
        fileBackedTasksManager.createEpic(epic1);
        Epic epic2 = new Epic(4, "Epic #2", "description2", NEW);
        fileBackedTasksManager.createEpic(epic2);

        Subtask subtask1 = new Subtask(5, "Subtask #1", "description1", NEW, epic1.getId(),
                Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        fileBackedTasksManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(6, "Subtask #2", "description2", NEW, epic1.getId());
        fileBackedTasksManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask(7, "Subtask #3", "description3", DONE, epic1.getId());
        fileBackedTasksManager.createSubtask(subtask3);
        return fileBackedTasksManager;
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
        return super.getTasks();
    }

    @Override
    public List<Epic> getEpics() {
        return super.getEpics();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return super.getSubtasks();
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
    public List<Task> getHistory() {
        List<Task> taskList = super.getHistory();
        save();
        return taskList;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void deleteAllTask() {
        super.deleteAllTask();
        save();
    }

    @Override
    public void deleteAllSubtask() {
        super.deleteAllSubtask();
        save();
    }

    @Override
    public void deleteAllEpic() {
        super.deleteAllEpic();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int epicId) {
        List<Subtask> subtasks = super.getAllEpicSubtasks(epicId);
        save();
        return subtasks;
    }
}