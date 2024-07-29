import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*

 * Уильям Столлингс в своей книге "Операционные Системы" предложил целых
 * три решения задачи с обедающими философами, чтобы не допустить взаимоблокировки:

 * 1. Купить еще пять вилок (самое подходящее решение задачи с точки зрения гигиены).
 * 2. Научить философов есть спагетти одной вилкой.
 * 3. Нанять вышибалу, который не позволит пяти философам садиться за стол одновременно.

 * По условию задачи первыми двумя методами воспользоваться мы не можем, и остается только третий.
 * Решить задачу с вышибалой Столлингс предлагает с использованием семафоров.

 * Семафор - это примитив синхронизации, являющийся своего рода "светофором" для наших потоков. Как только
 * значение семафора достигает 0, поток не может войти в пул потоков исполнения.

 * Семафор в предмете "Операционные Системы" обладал двумя методами:
 * verhogen (увеличение значение, acquire() в Java)
 * proberen (уменьшение значения, release() в Java)
 *
 * Пронумеруем философов. Будем считать, что номер каждой вилки соответствует номеру философа слева от вилки,
 * то есть, справа от философа под номером 2 лежит вилка под номером 2.
 *
 * Использованием семафора мы запрещаем пяти философам находиться за одним столом одновременно.
 * Значение permits (количество разрешений) семафора мы поставим равным 4, чтобы в случае, если пятый философ
 * хочет сесть за стол, наш вышибала ему запрещал.

 + Добавила условие с "официантом", который приносит вилки, только когда доступны и правая, и левая сразу

 * */
class DiningPhilosophers {
    private final Semaphore semaphore;
    private final ReentrantLock[] reentrantLocks;
    private final ReentrantLock philosophers;
    private final Condition condition;
    public DiningPhilosophers() {
        this.philosophers = new ReentrantLock(true);
        this.condition = philosophers.newCondition();
        this.reentrantLocks = new ReentrantLock[5];
        this.semaphore = new Semaphore(4);
        for (int i = 0; i < 5; i++) reentrantLocks[i] = new ReentrantLock(true);
    }
    public void wantsToEat(int philosopher,
                           Runnable pickLeftFork,
                           Runnable pickRightFork,
                           Runnable eat,
                           Runnable putLeftFork,
                           Runnable putRightFork)
            throws InterruptedException {

        // узнаем номер левой вилки. правая вилка = philosopher
        int leftForkId;
        if (philosopher == 0) leftForkId = 4;
        else leftForkId = philosopher--;

        // увеличиваем значение семафора. садимся за стол
        semaphore.acquire();

        // забираем себе все вилки в мире (ограничиваем доступ к общему ресурсу, блокируем других философов)
        philosophers.lock();

        // смотрим налево и направо на наши вилки, свободны ли они?
        while(reentrantLocks[leftForkId].isLocked() || reentrantLocks[philosopher].isLocked()) {

            // грустно смотрим, как философы справа и слева едят нашими вилками...
            condition.await();
        }

        // блокируем левую и правую вилки от захватчиков
        // (теперь только философ philosopher может пользоваться этими вилками)
        reentrantLocks[leftForkId].lock();
        reentrantLocks[philosopher].lock();

        // разрешаем брать оставшиеся вилки за столом другим голодным
        philosophers.unlock();

        // поднимаем левую и правую вилки, едим спагетти и убираем вилки
        pickLeftFork.run();
        pickRightFork.run();
        eat.run();
        putLeftFork.run();
        putRightFork.run();

        // разблокировываем левую и правую вилки. пусть кто-нибудь ещё поест
        reentrantLocks[leftForkId].unlock();
        reentrantLocks[philosopher].unlock();

        // нам нужно просигнались находящимся в очереди на вилки потокам, что вилки теперь свободны.
        // для этого забираем на секундочку условие опять и сигналим (иначе с Condition просигналить не получится)
        philosophers.lock();
        condition.signalAll();
        philosophers.unlock();


        // уменьшаем значение семафора. уходим думать
        semaphore.release();

    }
}
