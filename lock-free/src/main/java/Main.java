
public class Main implements Runnable {
    @Override
    public void run() {
        System.out.println("smth happens");
    }

    public static void main(String[] args) {

        SetImpl<Integer> set = new SetImpl<>();
        System.out.println("Does set contain 1? " + set.contains(1));
        System.out.println("Can we add 1? " + set.add(1));
        System.out.println("Does set contain 1? " + set.contains(1));
        System.out.println("Can we add 1? " + set.add(1));
        System.out.println("Can we add 2? " + set.add(2));
        System.out.println("Does set contain 2? " + set.contains(2));
        System.out.println("Is set empty? " + set.isEmpty());
        System.out.println("Can we remove 2? " + set.remove(2));
        System.out.println("Can we remove 1? " + set.remove(1));
        System.out.println("Can we remove 1? " + set.remove(1));
        System.out.println("Is set empty? " + set.isEmpty());
    }
}