package tasks;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected ArrayList<Subtask> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, StatusOfTask.valueOf("NEW"));
    }

    public Epic(String name, String description, int id) {
        super(name, description, StatusOfTask.valueOf("NEW"));
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
                ", statusOfTask='" + getStatusOfTask() + '\'' +
                ", subtaskList=" + subtaskIds +
                '}';
    }

    public ArrayList<Subtask> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(Subtask id) {
        if (id != null) {
            if (!subtaskIds.contains(id)) {
                subtaskIds.add(id);
            }
        }
    }

    public void removeSubTask(int subtaskId) {
        int removeIndex = -1;
        if (!(subtaskIds.isEmpty())) {
            for (Subtask taskNum : subtaskIds) {
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