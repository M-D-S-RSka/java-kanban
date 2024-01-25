import manager.*;
import tasks.*;

import static utilities.Status.*;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getInMemoryTaskManger();

        System.out.println("\n" + "Создание");
        Task task1 = new Task(1,"Task #1", "Task1 description", NEW);
        Task task2 = new Task(2,"Task #2", "Task2 description", IN_PROGRESS);
        final int taskId1 = manager.createTask(task1);
        final int taskId2 = manager.createTask(task2);


        Epic epic1 = new Epic(3,"Epic #1", "Epic1 description", NEW);
        Epic epic2 = new Epic(4,"Epic #2", "Epic2 description", NEW);
        final int epicId1 = manager.createEpic(epic1);
        final int epicId2 = manager.createEpic(epic2);

        //Subtask subtask1 = new Subtask("Subtask #1", "Subtask1 description", NEW, epicId1, 5, SUBTASK);
        Subtask subtask1 = new Subtask(5, "Subtask #1","Subtask1 description", NEW, epicId1);
        Subtask subtask2 = new Subtask(6,"Subtask #2", "Subtask2 description", NEW, epicId2);
        Subtask subtask3 = new Subtask(7,"Subtask #3", "Subtask3 description", DONE, epicId2);
        final int subtaskId1 = manager.createSubtask(subtask1);
        final int subtaskId2 = manager.createSubtask(subtask2);
        final int subtaskId3 = manager.createSubtask(subtask3);

        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("\n" + "Обновление");
        final Task task = manager.getTaskById(taskId2);
        task.setStatus(DONE);
        manager.getTaskById(taskId1);
        manager.getTaskById(taskId2);
        manager.getEpicById(epicId1);
        manager.getSubtaskById(subtaskId1);
        manager.getSubtaskById(subtaskId2);
        manager.getSubtaskById(subtaskId3);
        manager.getEpicById(epicId1);
        manager.getEpicById(epicId1);
        manager.getEpicById(epicId2);
        manager.getEpicById(epicId1);
        manager.getTaskById(taskId1);
        manager.getSubtaskById(subtaskId2);

        System.out.println("\n" + "Удаление");
        manager.deleteTaskById(taskId1);
        manager.deleteSubtaskById(subtaskId3);
        manager.deleteEpicById(epicId1);

        System.out.println("\n" + "История");
        for (Task tasktask : manager.getHistory()) {
            System.out.println(tasktask);
        }
    }
}