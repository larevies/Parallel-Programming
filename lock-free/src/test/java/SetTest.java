import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/* Lock-free algorithm definition:

An algorithm is lock-free if, when the program threads are run for a sufficiently long time,
at least one of the threads makes progress (for some sensible definition of progress).
All wait-free algorithms are lock-free.*/

// = если все потоки завершатся и не подерутся друг с другом за некоторое время,
// мы можем считать алгоритм lock-free.

// Первые четыре честа проверяют, выдерживает ли алгоритм несколько потоков, вторые четыре проверяют,
// правильно ли работают функции в тесте в принципе

public class SetTest {

    SetImpl<Integer> set = new SetImpl<>();
    Random rn = new Random();

    void addRoutine() {
        for (int i = 0; i < 100; i++) {
            int nextInt = rn.nextInt(5);
            set.add(nextInt);
        }
    }

    void removeRoutine() {
        for (int i = 0; i < 100; i++) {
            int nextInt = rn.nextInt(5);
            set.remove(nextInt);
        }
    }

    void containsRoutine() {
        for (int i = 0; i < 100; i++) {
            int nextInt = rn.nextInt(5);
            set.contains(nextInt);
        }
    }

    void isEmptyRoutine() {
        for (int i = 0; i < 100; i++) {
            set.isEmpty();
        }
    }

    @Test
    public void add() throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);

        Thread t1 = new Thread(this::addRoutine);
        Thread t2 = new Thread(this::addRoutine);
        Thread t3 = new Thread(this::addRoutine);

        cdl.countDown();

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        boolean awaited = cdl.await(20, TimeUnit.MILLISECONDS);
        boolean success = awaited && !t1.isAlive() && !t2.isAlive() && !t3.isAlive();

        assertTrue(success);
    }

    @Test
    public void remove() throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);

        Thread t1 = new Thread(this::addRoutine);
        Thread t2 = new Thread(this::removeRoutine);
        Thread t3 = new Thread(this::removeRoutine);

        cdl.countDown();

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        boolean awaited = cdl.await(20, TimeUnit.MILLISECONDS);
        boolean success = awaited && !t1.isAlive() && !t2.isAlive() && !t3.isAlive();

        assertTrue(success);
    }

    @Test
    public void contains() throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);

        Thread t1 = new Thread(this::addRoutine);
        Thread t2 = new Thread(this::containsRoutine);
        Thread t3 = new Thread(this::removeRoutine);

        cdl.countDown();

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        boolean awaited = cdl.await(20, TimeUnit.MILLISECONDS);
        boolean success = awaited && !t1.isAlive() && !t2.isAlive() && !t3.isAlive();

        assertTrue(success);
    }

    @Test
    public void isEmpty() throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);

        Thread t1 = new Thread(this::addRoutine);
        Thread t2 = new Thread(this::removeRoutine);
        Thread t3 = new Thread(this::isEmptyRoutine);
        Thread t4 = new Thread(this::removeRoutine);

        cdl.countDown();

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        boolean awaited = cdl.await(20, TimeUnit.MILLISECONDS);
        boolean success = awaited && !t1.isAlive() && !t2.isAlive() && !t3.isAlive();

        assertTrue(success);
    }

    @Test
    public void addGeneral() {
        assertTrue(set.add(1));
        assertFalse(set.add(1));
        assertTrue(set.add(2));
    }

    @Test
    public void removeGeneral() {
        set.add(1);
        set.add(2);
        set.add(3);
        assertTrue(set.remove(1));
        assertFalse(set.remove(1));
        assertTrue(set.remove(2));
    }

    @Test
    public void containsGeneral() {
        set.add(1);
        set.add(2);
        assertTrue(set.contains(1));
        assertFalse(set.contains(0));
        assertTrue(set.contains(2));
    }

    @Test
    public void isEmptyGeneral() {
        assertTrue(set.isEmpty());
        set.add(1);
        assertFalse(set.isEmpty());
        set.remove(1);
        assertTrue(set.isEmpty());
    }
}
