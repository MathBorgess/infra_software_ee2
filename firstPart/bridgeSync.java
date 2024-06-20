package firstPart;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bridge {
    private final Lock lock = new ReentrantLock();

    public void crossBridge(String direction, int carId) {
        lock.lock();
        try {
            System.out.println("Carro " + carId + " está atravessando a ponte e saiu da direção: " + direction);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Carro " + carId + " atravessou a ponte");
        } finally {
            lock.unlock();
        }
    }
}

class Car implements Runnable {
    private final String direction;
    private final int carId;
    private final Bridge bridge;

    public Car(String direction, int carId, Bridge bridge) {
        this.direction = direction;
        this.carId = carId;
        this.bridge = bridge;
    }

    @Override
    public void run() {
        bridge.crossBridge(direction, carId);
    }
}

public class bridgeSync {
    public static void main(String[] args) {
        Bridge bridge = new Bridge();
        for (int i = 1; i < 15; i++) {
            String direction = (i % 2 == 0) ? "Esquerda" : "Direita";
            Thread car = new Thread(new Car(direction, i, bridge));
            car.start();
        }
    }
}
