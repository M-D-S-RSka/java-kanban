package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLinkedList {
    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node first;
    private Node last;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        private Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node currentNode = first;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    public void linkLast(Task task) {
        final Node oldTail = last;
        final Node newTail = new Node(task, oldTail, null);
        last = newTail;
        if (oldTail == null) {
            first = newTail;
        } else {
            oldTail.next = newTail;
        }
        nodeMap.put(task.getId(), newTail);
    }

    public void removeNode(int id) {
        final Node node = nodeMap.remove(id);
        if (node == null) {
            return;
        }
        if (node == first) {
            first = node.next;
        }
        if (node == last) {
            last = node.prev;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        node.prev = null;
        node.next = null;
    }

    Map<Integer, Node> getNodeMap() {
        return nodeMap;
    }
}
