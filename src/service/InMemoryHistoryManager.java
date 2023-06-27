package service;

import model.Node;
import model.Task;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> historyTasks = new CustomLinkedList<>();
    //private final Map<Integer, Node> historyView = new HashMap<>();

    @Override
    public void add(Task task) {
        historyTasks.linkLast(task);
    };

    @Override
    public void remove(int id) {
        if (!historyTasks.historyView.isEmpty() && historyTasks.historyView.containsKey(id)) {
            Node<Task> node = historyTasks.historyView.get(id);
            historyTasks.removeNode(node);
            historyTasks.historyView.remove(id);
        }
    };

    @Override
    public List<Task> getHistory() {
        return historyTasks.getTasks();
    };

    public class CustomLinkedList<T extends Task> {
        private final Map<Integer, Node<T>> historyView = new HashMap<>();
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        public int getSize() {
            return size;
        }

        public void linkLast (T task) {
            int taskId = task.getId();
            final Node<T> oldTail = tail;
            final Node<T> newNode = new Node<>(oldTail, task, null);
            tail = newNode;

            if (oldTail == null)
                head = newNode;
            else
                oldTail.next = newNode;

            if (historyView.containsKey(taskId)) {
                remove(taskId);
            } else {
                size++;
            }

            historyView.put(taskId, newNode);
        }

        public List<T> getTasks () {
            List<T> tasks = new ArrayList<>();

            if (size > 0) {
                for (Node<T> value : historyView.values()) {
                    tasks.add(value.element);
                }
            }

            return tasks;
        }

        public void removeNode (Node<T> node){
            if (size > 0 && node != null) {
                if (head == node) {
                    head = head.next;
                }
                if (tail == node) {
                    tail = tail.prev;
                }
                if (node.prev != null) {
                    node.prev.next = node.next;
                }
                if (node.next != null) {
                    node.next.prev = node.prev;
                }
            }
        }
    }
}
