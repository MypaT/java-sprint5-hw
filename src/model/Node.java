package model;

public class Node<T extends Task>  {
    public T element;
    public Node<T> prev;
    public Node<T> next;

    public Node(Node<T> prev, T element, Node<T> next) {
        this.prev = prev;
        this.element = element;
        this.next = next;
    }
}
