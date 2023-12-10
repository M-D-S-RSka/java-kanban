package tasks;

import java.util.Objects;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(String name, String description, TaskStatus statusOfTask, int epicId, int id, Type type) {
        super(name, description, statusOfTask, id, type);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus statusOfTask, int epicId, Type type) {
        super(name, description, statusOfTask, type);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
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

    @Override
    public String taskToString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", statusOfTask='" + getStatus() + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}