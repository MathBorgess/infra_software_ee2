class BridgeWithoutControl {
    public void crossBridge(String direction, int carId) {
        System.out.println("Carro " + carId + " está atravessando a ponte e saiu da direção: " + direction);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Carro " + carId + " atravessou a ponte");
    }
}

class CarWithoutControl implements Runnable {
    private final String direction;
    private final int carId;
    private final BridgeWithoutControl bridge;

    public CarWithoutControl(String direction, int carId, BridgeWithoutControl bridge) {
        this.direction = direction;
        this.carId = carId;
        this.bridge = bridge;
    }

    @Override
    public void run() {
        bridge.crossBridge(direction, carId);
    }
}

public class bridgeWithoutSync {
    public static void main(String[] args) {
        BridgeWithoutControl bridge = new BridgeWithoutControl();
        Thread previousCar = null;
        
        for (int i = 0; i < 10; i++) {
            String direction = (i % 2 == 0) ? "Esquerda" : "Direita";
            Thread car = new Thread(new CarWithoutControl(direction, i, bridge));

            if (previousCar != null) {
                try {
                    previousCar.join(); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            car.start();
            previousCar = car; 
        }
    }
}
