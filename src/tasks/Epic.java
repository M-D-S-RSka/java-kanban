package tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

import static tasks.Type.EPIC;

public class Epic extends Task {
    protected List<Integer> subtaskIds = new ArrayList<>();
    protected LocalDateTime endTime;

    public Epic(Integer id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Epic(Integer id, String name, String description, Status status, Duration duration, LocalDateTime startTime,
                LocalDateTime endTime) {
        super(id, name, description, status, duration, startTime);
        this.endTime = endTime;
    }

    public Epic(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
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
        if (startTime != null) {
            result = result +
                    ", duration=" + duration +
                    ", startTime=" + startTime;
        }

        return result + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(subtaskIds, epic.subtaskIds) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime);
    }
}