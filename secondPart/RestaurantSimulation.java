import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class Restaurant {
    private final int CAPACITY = 5;
    private final Lock lock = new ReentrantLock();
    private final Condition allLeave = lock.newCondition();
    private List<Customer> seats = new ArrayList<>(CAPACITY);
    private Queue<Customer> waitingQueue = new LinkedList<>();
    private CyclicBarrier barrier = new CyclicBarrier(CAPACITY, new Runnable() {
        public void run() {
            releaseCustomers();
        }
    });

    public void sit(Customer customer) throws InterruptedException {
        lock.lock();
        try {
            if (seats.size() == CAPACITY) {
                System.out.println(customer.getCustomerName() + " está esperando na fila.");
                waitingQueue.add(customer);
                while (waitingQueue.peek() != customer || seats.size() == CAPACITY) {
                    allLeave.await();
                }
                waitingQueue.poll();
            }
            seats.add(customer);
            System.out.println(customer.getCustomerName() + " sentou. Lugares que estão ocupados: " + seats.size());
            if (seats.size() == CAPACITY) {
                System.out.println("Restaurante está cheio!!!!");
            }
        } finally {
            lock.unlock();
        }
    }

    public void leave(Customer customer) {
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCustomers() {
        lock.lock();
        try {
            seats.clear();
            System.out.println("Todos os clientes saíram.");
            allLeave.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class Customer extends Thread {
    private final String name;
    private final Restaurant restaurant;

    public Customer(String name, Restaurant restaurant) {
        this.name = name;
        this.restaurant = restaurant;
    }

    public String getCustomerName() {
        return name;
    }

    @Override
    public void run() {
        try {
            restaurant.sit(this);
            Thread.sleep(1000);
            restaurant.leave(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class RestaurantSimulation {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        for (int i = 1; i <= 100; i++) {
            Customer customer = new Customer("Cliente " + i, restaurant);
            customer.start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
