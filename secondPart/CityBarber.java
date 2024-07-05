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

class BarberCustomer extends Thread {

    @Override
    public void run() {
        try {
            System.out.println(getName() + " chegou à barbearia.");
            boolean isChairAvaliable = CityBarber.chairSemaphore.tryAcquire(0, TimeUnit.SECONDS);
            if (isChairAvaliable) {
                System.out.println(getName() + " sentou e está esperando para cortar o cabelo.");

                boolean isBarberSleeping = CityBarber.barberSemaphore.tryAcquire(0, TimeUnit.SECONDS);
                if (isBarberSleeping) {
                    System.out.println(getName() + " acordou o barbeiro e está cortando o cabelo.");
                } else {
                    System.out.println(
                            getName() + " está esperando o barbeiro terminar de cortar o cabelo de outro cliente.");
                    CityBarber.barberSemaphore.acquire();
                    System.out.println(getName() + " está cortando o cabelo com o barbeiro");
                }
                Random rand = new Random();
                int time = 1000 * rand.nextInt(5);
                Thread.sleep(time);
                System.out.println(getName() + " cortou o cabelo em " + time + " ms e está saindo da barbearia.");
                CityBarber.barberSemaphore.release();

                CityBarber.chairSemaphore.release();
                System.out.println(getName() + " saiu.");
            } else {
                System.out.println(getName() + " não conseguiu sentar e está saindo.");
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

    public static void main(String[] args) throws InterruptedException {
        Random rand = new Random();
        CityBarber.chairs = rand.nextInt(10) + 1;
        CityBarber.chairSemaphore = new Semaphore(CityBarber.chairs);
        System.out.println("Cadeiras disponíveis " + CityBarber.chairs);

        for (int i = 0; i < 100; i++) {
            BarberCustomer thread = new BarberCustomer();
            thread.start();

            Thread.sleep(200 * rand.nextInt(10));
        }
    }
}
