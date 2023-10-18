package tasks;

import java.util.Objects;

public class Task {

    protected Integer taskId;
    protected String name;
    protected StatusOfTask statusOfTask;
    protected String description;

    public Task(String name, String description, StatusOfTask statusOfTask, Integer taskId) {
        this.name = name;
        this.statusOfTask = statusOfTask;
        this.description = description;
        this.taskId = taskId;
    }

    public Task(String name, String description, StatusOfTask statusOfTask) {
        this.name = name;
        this.statusOfTask = statusOfTask;
        this.description = description;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Task task = (Task) object;
        return Objects.equals(taskId, task.taskId) && Objects.equals(name, task.name)
                && Objects.equals(description, task.description) && Objects.equals(statusOfTask, task.statusOfTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, taskId, statusOfTask);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name ='" + name + '\'' +
                ", description ='" + description + '\'' +
                ", taskId =" + taskId +
                ", statusOfTask ='" + statusOfTask + '\'' +
                '}';
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public StatusOfTask getStatusOfTask() {
        return statusOfTask;
    }

    public void setStatusOfTask(StatusOfTask statusOfTask) {
        this.statusOfTask = statusOfTask;
    }

    public String getDescription() {
        return description;
    }
}