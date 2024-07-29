import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T> {
    T value;
    final AtomicMarkableReference<Node<T>> next;

    public Node(T value, Node<T> next) {
        this.value = value;
        this.next = new AtomicMarkableReference<>(next, false);
    }
}
