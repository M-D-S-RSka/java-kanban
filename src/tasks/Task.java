package tasks;

import java.util.Objects;

public class Task {

    protected Integer id;
    protected String name;
    protected TaskStatus status;
    protected String description;

    public Task(String name, String description, TaskStatus statusOfTask, Integer taskId) {
        this.name = name;
        this.status = statusOfTask;
        this.description = description;
        this.id = taskId;
    }

    public Task(String name, String description, TaskStatus statusOfTask) {
        this.name = name;
        this.status = statusOfTask;
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
        return Objects.equals(id, task.id) && Objects.equals(name, task.name)
                && Objects.equals(description, task.description) && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name ='" + name + '\'' +
                ", description ='" + description + '\'' +
                ", id =" + id +
                ", statusOfTask ='" + status + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}