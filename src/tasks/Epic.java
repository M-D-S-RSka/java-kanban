package tasks;

import utilities.Status;
import utilities.Type;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

import static utilities.Type.EPIC;

public class Epic extends Task {
    protected List<Integer> subtaskIds = new ArrayList<>();

    public Epic(Integer id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Epic(Integer id, String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
    }

    public Epic(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    @Override
    public LocalDateTime getEndTime() {
        return super.getEndTime();
    }


    @Override
    public Type getType() {
        return EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", statusOfTask='" + getStatus() + '\'' +
                ", subtaskList=" + subtaskIds +
                '}';
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskIds(Integer id) {
        subtaskIds.add(id);
    }

    public void removeSubtask(Integer subtaskId) {
        int removeIndex = -1;
        if (!(subtaskIds.isEmpty())) {
            for (Integer taskNum : subtaskIds) {
                if (taskNum.equals(subtaskId)) {
                    removeIndex = subtaskIds.indexOf(taskNum);
                }
            }
        }
        if (removeIndex != -1) {
            subtaskIds.remove(removeIndex);
        }
    }

    public void clearSubtask() {
        subtaskIds.clear();
    }

    @Override
    public String taskToString() {
        String result = "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status;
        if (!subtaskIds.isEmpty()) {
            result = result + ", subtaskIds=" + subtaskIds;
        }
        if (startTime != LocalDateTime.MAX) {
            result = result +
                    ", duration=" + duration +
                    ", startTime=" + startTime;
        }

        return result + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }
}