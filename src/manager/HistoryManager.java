package manager;

import tasks.*;

import java.util.List;

public interface HistoryManager {
    List<Task> getHistory();

    void addTask(Task task);

    void remove(int id);

    default void deleteAll() {
        while (!getHistory().isEmpty()) {
            remove(getHistory().get(0).getId());
        }
    }
}