import manager.TasksManager;
import tasks.*;

public class Main {

    /* Приветствую) попытался реализовать public enum StatusOfTask, читал в гугле про него,
    поэтому есть подозрения, что грамотно не реализовал, будто масло масляное в коде со статусами вышло... Если есть
    какой комменатрий по этому поводу, да и в целом, конечно, то жду :) Т к мне кажется, что через отдельный класс со
    статусами более прагматично чтоль получилось, но выходит как то громоздко, стоило ли... */

    public static void main(String[] args) {
        TasksManager taskManager = getTasksManager();
        System.out.println(taskManager.getAllEpic());
        System.out.println(taskManager.getAllTask());
        System.out.println(taskManager.getAllSubTask());
        Subtask subtask4 = new Subtask("SUBTASK4", "DESCRIPTION", StatusOfTask.getStatus("DONE"),
                2, 4);
        taskManager.updateSubtask(subtask4);
        taskManager.deleteTaskById(4);
        System.out.println(taskManager.getEpicById(2));
        Task task2 = new Task("TASK2", "DESCRIPTION", StatusOfTask.getStatus("DONE"), 1);
        taskManager.updateTask(task2);
        System.out.println(taskManager.getAllEpicSubTasks(3));
        System.out.println(taskManager.getAllEpic());
        Subtask subtask5 = new Subtask("SUBTASK5", "DESCRIPTION", StatusOfTask.getStatus("DONE"),
                3, 6);
        taskManager.updateSubtask(subtask5);
        System.out.println(taskManager.getAllEpic());
    }

    private static TasksManager getTasksManager() {
        TasksManager taskManager = new TasksManager();

        Task task = new Task("TASK", "DESCRIPTION", StatusOfTask.getStatus("NEW"));
        taskManager.createTask(task);
        Epic epic = new Epic("EPIC", "DESCRIPTION");
        taskManager.createEpic(epic);
        Epic epic2 = new Epic("EPIC2", "DESCRIPTION");
        taskManager.createEpic(epic2);
        Subtask subtask = new Subtask("SUBTASK", "DESCRIPTION", StatusOfTask.getStatus("NEW"),
                2);
        Subtask subtask2 = new Subtask("SUBTASK2", "DESCRIPTION", StatusOfTask.getStatus("NEW"),
                3);
        Subtask subtask3 = new Subtask("SUBTASK3", "DESCRIPTION", StatusOfTask.getStatus("NEW"),
                3);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        return taskManager;
    }
}
