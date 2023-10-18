package tasks;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected ArrayList<Integer> subtaskList = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, StatusOfTask.valueOf("NEW"));
    }

    public Epic(String name, String description, int taskId) {
        super(name, description, StatusOfTask.valueOf("NEW"));
        this.setTaskId(taskId);
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
        return Objects.equals(subtaskList, epic.subtaskList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtaskList);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", taskId=" + getTaskId() +
                ", statusOfTask='" + getStatusOfTask() + '\'' +
                ", subtaskList=" + subtaskList +
                '}';
    }

    public ArrayList<Integer> getSubtaskList() {
        return subtaskList;
    }

    public void checkSubtask(Subtask task) {
        if (task != null) {
            if (task.getEpicId() == this.getTaskId()) {
                if (!subtaskList.contains(task.getTaskId())) subtaskList.add(task.getTaskId());
            }
        }

    }

    public void removeSubTask(Integer subtaskId) {
        int removeIndex = -1;
        if (!(subtaskList.isEmpty())) {
            for (Integer taskNum : subtaskList) {
                if (taskNum.equals(subtaskId)) {
                    removeIndex = subtaskList.indexOf(taskNum);
                }
            }
        }
        if (removeIndex != -1) {
            subtaskList.remove(removeIndex);
        }
    }

    public void clearSubtask() {
        subtaskList.clear();
    }
}