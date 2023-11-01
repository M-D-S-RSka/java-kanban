package tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    protected List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public Epic(String name, String description, int id) {
        super(name, description, TaskStatus.NEW);
        this.setId(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Epic epic = (Epic) object;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtaskIds);
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

    public void addSubtaskId(Subtask task) {
        if (task != null) {
            if (task.getEpicId() == this.getId()) {
                if (!subtaskIds.contains(task.getId())) {
                    subtaskIds.add(task.getId());
                }
            }
        }
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
}