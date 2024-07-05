package secondPart;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Restaurant {
    private final int CAPACITY = 5;
    private final Lock lock = new ReentrantLock();
    private final Condition allLeave = lock.newCondition();
    private final List<RestaurantCustomer> seats = new ArrayList<>(CAPACITY);
    private final Queue<RestaurantCustomer> waitingQueue = new LinkedList<>();
    private final CyclicBarrier barrier = new CyclicBarrier(CAPACITY, new Runnable() {
        public void run() {
            releaseCustomers();
        }
    });

    public Restaurant() {
    }

    public void sit(RestaurantCustomer customer) throws InterruptedException {
        lock.lock();
        try {
            if (seats.size() == CAPACITY) {
                System.out.println(customer.getName() + " está esperando na fila.");
                waitingQueue.add(customer);
                while (waitingQueue.peek() != customer || seats.size() == CAPACITY) {
                    allLeave.await();
                }
                waitingQueue.poll();
            }
            seats.add(customer);
            System.out.println(customer.getName() + " sentou. Lugares que estão ocupados: " + seats.size());
            if (seats.size() == CAPACITY) {
                System.out.println("Restaurante está cheio!!!!");
            }
        } finally {
            lock.unlock();
        }
    }

    public void leave(RestaurantCustomer customer) {
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

class RestaurantCustomer extends Thread {

    public RestaurantCustomer() {
    }

    @Override
    public void run() {
        try {
            RestaurantSimulation.restaurant.sit(this);
            Thread.sleep(1000);
            RestaurantSimulation.restaurant.leave(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class RestaurantSimulation {
    public static Restaurant restaurant = new Restaurant();

    public static void main(String[] args) {
        System.out.println("Iniciando simulação do restaurante.");
        for (int i = 1; i < 101; i++) {
            System.out.println("Cliente " + i + " chegou.");
            RestaurantCustomer customer = new RestaurantCustomer();
            customer.start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
