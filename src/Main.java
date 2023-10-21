import manager.TasksManager;
import tasks.*;

public class Main {

    public static void main(String[] args) {
        TasksManager taskManager = getTasksManager();
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubtasks());
        Subtask subtask4 = new Subtask("SUBTASK4", "DESCRIPTION", StatusOfTask.DONE,
                2, 4);
        taskManager.updateSubtask(subtask4);
        taskManager.deleteTaskById(4);
        System.out.println(taskManager.getEpicById(2));
        Task task2 = new Task("TASK2", "DESCRIPTION", StatusOfTask.DONE, 1);
        taskManager.updateTask(task2);
        System.out.println(taskManager.getAllEpicSubtasks(3));
        System.out.println(taskManager.getEpics());
        Subtask subtask5 = new Subtask("SUBTASK5", "DESCRIPTION", StatusOfTask.DONE,
                3, 6);
        taskManager.updateSubtask(subtask5);
        System.out.println(taskManager.getEpics());
    }

    private static TasksManager getTasksManager() {
        TasksManager taskManager = new TasksManager();

        Task task = new Task("TASK", "DESCRIPTION", StatusOfTask.NEW);
        taskManager.createTask(task);
        Epic epic = new Epic("EPIC", "DESCRIPTION");
        taskManager.createEpic(epic);
        Epic epic2 = new Epic("EPIC2", "DESCRIPTION");
        taskManager.createEpic(epic2);
        Subtask subtask = new Subtask("SUBTASK", "DESCRIPTION", StatusOfTask.NEW,
                2);
        Subtask subtask2 = new Subtask("SUBTASK2", "DESCRIPTION", StatusOfTask.NEW,
                3);
        Subtask subtask3 = new Subtask("SUBTASK3", "DESCRIPTION", StatusOfTask.NEW,
                3);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        return taskManager;
    }
}