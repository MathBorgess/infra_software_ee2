package secondPart;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Uma barbearia consiste em uma sala de espera com n cadeiras e
 * a sala do barbeiro contendo a cadeira do barbeiro.
 * 
 * Se não houver clientes para atender, o barbeiro vai dormir.
 * Se um cliente entra na barbearia e todas as cadeiras estão ocupadas, o
 * cliente sai da loja.
 * Se o barbeiro estiver ocupado, mas houver cadeiras disponíveis, o cliente
 * senta-se em uma das cadeiras livres. Se o barbeiro estiver dormindo, o
 * cliente acorda o barbeiro. Escreva um programa para coordenar o barbeiro e os
 * clientes.
 * 
 * Observações:
 * Dica: Dá pra simular o tempo de descanso do barbeiro utilizando o sleep.
 * Crie uma thread para cada cliente.
 * 
 */

class Customer extends Thread {

    @Override
    public void run() {
        try {
            System.out.println(getName() + " triying to acquire a chair.");
            boolean isChairAvaliable = CityBarber.chairSemaphore.tryAcquire(0, TimeUnit.SECONDS);
            if (isChairAvaliable) {
                System.out.println(getName() + " acquire a chair to wait.");
                CityBarber.barberSemaphore.acquire();
                System.out.println(getName() + " cutting its hair");
                System.out.println(getName() + " cutted its hair");
                CityBarber.barberSemaphore.release();
                CityBarber.chairSemaphore.release();
                System.out.println(getName() + " releasing its chair");
            } else {
                System.out.println(getName() + " leaving the barbershop without cutting its hair");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class CityBarber {

    public static Semaphore chairSemaphore = new Semaphore(1);
    public static int chairs = 1;
    public static Semaphore barberSemaphore = new Semaphore(1);

    public CityBarber(int chairs) {
        // CityBarber.chairSitSemaphore = new Semaphore(chairs);
        CityBarber.chairs = chairs;
    }

    public static void main(String[] args) {

        Random rand = new Random();
        CityBarber.chairs = rand.nextInt(5) + 1;
        CityBarber.chairSemaphore = new Semaphore(CityBarber.chairs);
        System.out.println("chairs avaliable " + CityBarber.chairs);

        for (int i = 0; i < rand.nextInt(10) + CityBarber.chairs + 1; i++) {
            Customer thread = new Customer();
            thread.start();
        }
    }
}
