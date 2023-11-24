package manager;

import tasks.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    CustomLinkedList customLinkedList = new CustomLinkedList();

    @Override
    public void addTask(Task task) {
        if (customLinkedList.getNodeMap().containsKey(task.getId())) {
            remove(task.getId());
        }
        customLinkedList.linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return customLinkedList.getTasks();
    }

    @Override
    public void remove(int id) {
        customLinkedList.removeNode(id);
    }
}