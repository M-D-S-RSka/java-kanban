package tasks;

import java.util.Objects;

public class Subtask extends Task {
    protected int epicId;

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", taskId=" + getTaskId() +
                ", statusOfTask='" + getStatusOfTask() + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    public Subtask(String name, String description, StatusOfTask statusOfTask, int epicId, int taskId) {
        super(name, description, statusOfTask);
        this.epicId = epicId;
        this.setTaskId(taskId);
    }

    public Subtask(String name, String description, StatusOfTask statusOfTask, int epicId) {
        super(name, description, statusOfTask);
        this.epicId = epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        Subtask subtask = (Subtask) object;
        return epicId == subtask.epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}