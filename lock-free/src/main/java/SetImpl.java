
public class SetImpl<T extends Comparable<T>> implements Set<T> {

    private final Node<T> head = new Node<>(null, null);

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean add(T value) {
        while (true) {

            Node<T>[] nodes = find(value);
            Node<T> pred = nodes[0], curr = nodes[1];

            if (curr != null && curr.value.compareTo(value) == 0) return false;

            Node<T> newNode = new Node<>(value, curr);

            // если pred == curr, то заменяем на newNode. curr не должна быть помечена
            // (не удалена логически)
            if (pred.next.compareAndSet(curr, newNode, false, false)) {
                return true;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean remove(T value) {
        while(true) {

            Node<T>[] nodes = find(value);
            Node<T> pred = nodes[0], curr = nodes[1];

            if (curr == null || curr.value.compareTo(value) != 0) return false;

            Node<T> next = curr.next.getReference();

            // Удаление в 2 фазы:
            // 1. Логическое удаление
            if (!curr.next.compareAndSet(next, next, false, true)) continue;

            // 2. Физическое удаление из списка
            pred.next.compareAndSet(curr, next, false, false);

            return true;
        }
    }

    @Override
    public boolean contains(T value) {

        Node<T> curr = head.next.getReference();

        while (curr != null) {
            if (curr.value.compareTo(value) == 0 && !curr.next.isMarked()) return true;
            curr = curr.next.getReference();
        }
        return false;
    }

    // Больше никаких size :c
    // (потому что наши ключи могут быть уже удалены... логически)
    @Override
    public boolean isEmpty() {
        Node<T> curr = head.next.getReference();
        while (curr != null) {
            if (!curr.next.isMarked()) {
                return false;
            } else {
                head.next.compareAndSet(curr, curr.next.getReference(), false, false);
            }
            curr = head.next.getReference();
        }
        return true;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked", "SpellCheckingInspection"})
    private Node<T>[] find(T value) {

        retry:
        while (true) {
            Node<T> prev = head;
            Node<T> curr = prev.next.getReference();
            Node<T> succ;

            while (curr != null) {
                succ = curr.next.getReference();
                boolean cmk = curr.next.isMarked();

                // если ключ удален логически??
                if (cmk) {
                    if (prev.next.compareAndSet(curr, succ, false, false)) {
                        continue retry;
                    }
                } else {
                    if (curr.value.compareTo(value) == 0) return new Node[]{prev, curr};
                    prev = curr;
                }
                curr = succ;
            }
            return new Node[]{prev, curr};
        }
    }
}

