package manager;

import tasks.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void addTask(Task task) {
        Integer taskId = task.getId();

        if (nodeMap.containsKey(taskId)) {
            Node node = nodeMap.remove(taskId);
            removeNode(node);
        }
        linkLast(task);
        nodeMap.put(taskId, tail);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (nodeMap.containsKey(id)) {
            Node node = nodeMap.remove(id);
            removeNode(node);
        }
    }

    public static class Node {
        public Task item;
        public Node next;
        public Node prev;

        public Node(Task item, Node next, Node prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    private void removeNode(Node node) {
        final Node prev = node.prev;
        final Node next = node.next;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }
        node.item = null;
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(task, null, null);

        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node node = head;

        while (node != null) {
            tasks.add(node.item);
            node = node.next;
        }
        return tasks;
    }
}