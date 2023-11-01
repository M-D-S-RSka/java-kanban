package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> listOfHistory = new ArrayList<>();

    @Override
    public void addTask(Task task) {
        if (listOfHistory.size() >= 10) {
            listOfHistory.remove(0);
        }
        listOfHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return listOfHistory;
    }
}